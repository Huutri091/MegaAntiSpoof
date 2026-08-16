package vn.megacitymc.megaantispoof;

import vn.megacitymc.megaantispoof.api.DetectionResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

final class WebhookClient {
    private final MegaAntiSpoofPlugin plugin;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    WebhookClient(MegaAntiSpoofPlugin plugin) {
        this.plugin = plugin;
    }

    void send(DetectionResult result) {
        if (!plugin.getConfig().getBoolean("webhook.enabled",
                plugin.getConfig().getBoolean("webhook.bat", false))) return;
        String url = plugin.getConfig().getString("webhook.url", "");
        if (url == null || url.isBlank()) return;

        String serverName = plugin.getConfig().getString("webhook.server-name",
                plugin.getConfig().getString("webhook.may-chu",
                plugin.getConfig().getString("server-name",
                plugin.getConfig().getString("may-chu", "MegaCity"))));

        String payload = buildPayload(result, serverName);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.discarding()).thenAccept(response -> {
            if (response.statusCode() / 100 != 2) {
                plugin.getLogger().warning("Webhook Discord trả mã " + response.statusCode());
            }
        }).exceptionally(error -> {
            plugin.getLogger().warning("Gửi webhook thất bại: " + error.getMessage());
            return null;
        });
    }

    String buildPayload(DetectionResult result, String serverName) {
        int color;
        String resultText;
        switch (result.status()) {
            case PASSED -> {
                color = 5763719; // Green #57F287
                resultText = "PASSED";
            }
            case FAILED -> {
                color = 15548997; // Red #ED4245
                resultText = "FAILED";
            }
            case PROTECTED -> {
                color = 16705372; // Yellow #FEE75C
                resultText = "PROTECTED";
            }
            default -> {
                color = 10066329; // Gray #99AAB5
                resultText = "ERROR";
            }
        }

        List<DetectionResult.CheckedMod> checked = result.checkedMods();
        int totalMods = checked.size();
        int detectedCount = 0;
        for (DetectionResult.CheckedMod mod : checked) {
            if (mod.detected()) detectedCount++;
        }
        if (totalMods == 0 && !result.detectedMods().isEmpty()) {
            detectedCount = result.detectedMods().size();
        }

        StringBuilder modList = new StringBuilder();
        if (checked.isEmpty()) {
            if (result.detectedMods().isEmpty()) {
                modList.append("Không có dữ liệu mod");
            } else {
                for (int i = 0; i < result.detectedMods().size(); i++) {
                    if (i > 0) modList.append("\n");
                    modList.append("**").append(result.detectedMods().get(i)).append("**: Phát hiện");
                }
            }
        } else {
            for (int i = 0; i < checked.size(); i++) {
                DetectionResult.CheckedMod mod = checked.get(i);
                if (i > 0) modList.append("\n");
                modList.append("**").append(mod.name()).append("**: ")
                        .append(mod.detected() ? "Phát hiện" : "Không phát hiện");
            }
        }

        String scanDetailTitle = "Chi tiết Quét (" + detectedCount + "/" + totalMods + " Phát hiện)";
        String detailValue = modList.toString();
        if (detailValue.length() > 1024) {
            detailValue = detailValue.substring(0, 1020) + "...";
        }

        String timestamp = result.timestamp() != null ? result.timestamp().toString() : Instant.now().toString();
        String playerName = result.playerName() != null ? result.playerName() : "Không xác định";
        String uuid = result.playerId() != null ? result.playerId().toString() : "N/A";
        String ip = (result.playerIp() != null && !result.playerIp().isBlank()) ? result.playerIp() : "Không xác định";

        StringBuilder json = new StringBuilder(1024);
        json.append("{\"username\":\"MegaAntiSpoof\",\"embeds\":[{");
        json.append("\"title\":\"Kết quả kiểm tra bảo mật\",");
        json.append("\"color\":").append(color).append(",");
        json.append("\"timestamp\":\"").append(esc(timestamp)).append("\",");
        json.append("\"fields\":[");
        json.append("{\"name\":\"Tài khoản\",\"value\":\"").append(esc(playerName)).append("\",\"inline\":true},");
        json.append("{\"name\":\"UUID\",\"value\":\"").append(esc(uuid)).append("\",\"inline\":true},");
        json.append("{\"name\":\"IP\",\"value\":\"").append(esc(ip)).append("\",\"inline\":true},");
        json.append("{\"name\":\"Máy chủ\",\"value\":\"").append(esc(serverName)).append("\",\"inline\":true},");
        json.append("{\"name\":\"Kết quả\",\"value\":\"").append(esc(resultText)).append("\",\"inline\":true},");
        json.append("{\"name\":\"").append(esc(scanDetailTitle)).append("\",\"value\":\"").append(esc(detailValue)).append("\",\"inline\":false}");
        json.append("],");
        json.append("\"footer\":{\"text\":\"MegaAntiSpoof\"}");
        json.append("}]}");
        return json.toString();
    }

    private static String esc(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}

