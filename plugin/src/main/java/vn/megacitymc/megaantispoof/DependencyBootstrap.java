package vn.megacitymc.megaantispoof;

import org.bukkit.Bukkit;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.security.*;
import java.time.Duration;
import java.util.*;

final class DependencyBootstrap {
    private static final List<Dependency> DEPENDENCIES = List.of(
            new Dependency("ProtocolLib", "ProtocolLib-5.4.0.jar",
                    "https://repo1.maven.org/maven2/net/dmulloy2/ProtocolLib/5.4.0/ProtocolLib-5.4.0-all.jar", 5_000_000L),
            new Dependency("packetevents", "packetevents-spigot-2.13.0.jar",
                    "https://github.com/retrooper/packetevents/releases/download/v2.13.0/packetevents-spigot-2.13.0.jar", 1_000_000L)
    );

    private final MegaAntiSpoofPlugin plugin;
    DependencyBootstrap(MegaAntiSpoofPlugin plugin) { this.plugin = plugin; }

    boolean ensureInstalled() {
        if (!plugin.getConfig().getBoolean("dependencies.auto-download",
                plugin.getConfig().getBoolean("phu-thuoc.tu-dong-tai", true))) return allPresent();
        List<Dependency> missing = DEPENDENCIES.stream().filter(d -> !present(d.pluginName)).toList();
        if (missing.isEmpty()) return true;
        plugin.getLogger().warning("Thiếu phụ thuộc: " + missing.stream().map(d -> d.pluginName).toList());
        plugin.getLogger().warning("Đang tải từ nguồn chính thức; MegaAntiSpoof sẽ hoạt động sau khi khởi động lại server.");
        Thread.ofVirtual().name("MegaAntiSpoof-dependency-installer").start(() -> downloadAll(missing));
        return false;
    }

    private void downloadAll(List<Dependency> missing) {
        Path pluginDirectory = plugin.getDataFolder().toPath().toAbsolutePath().getParent();
        if (pluginDirectory == null) { plugin.getLogger().severe("Không xác định được thư mục plugins."); return; }
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10)).build();
        boolean success = true;
        for (Dependency dependency : missing) {
            Path destination = pluginDirectory.resolve(dependency.fileName).normalize();
            if (!destination.getParent().equals(pluginDirectory)) {
                plugin.getLogger().severe("Đích phụ thuộc không an toàn: " + destination); success = false; continue;
            }
            Path temporary = pluginDirectory.resolve("." + dependency.fileName + ".download");
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(dependency.url)).timeout(Duration.ofSeconds(90))
                        .header("User-Agent", "MegaAntiSpoof/1.0.0").GET().build();
                HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(temporary,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
                if (response.statusCode() / 100 != 2) throw new IOException("HTTP " + response.statusCode());
                validateJar(temporary, dependency.minimumBytes);
                String depKey = dependency.pluginName.toLowerCase(Locale.ROOT);
                String expected = plugin.getConfig().getString("dependencies.sha256." + depKey,
                        plugin.getConfig().getString("phu-thuoc.sha256." + depKey, ""));
                if (expected != null && !expected.isBlank() && !sha256(temporary).equalsIgnoreCase(expected.strip()))
                    throw new SecurityException("SHA-256 không khớp");
                try { Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE); }
                catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, destination); }
                plugin.getLogger().info("Đã cài " + dependency.pluginName + " -> " + destination.getFileName());
            } catch (Exception ex) {
                success = false;
                plugin.getLogger().severe("Không tải được " + dependency.pluginName + ": " + ex.getMessage());
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            }
        }
        if (success) plugin.getLogger().warning("Đã tải đủ phụ thuộc. Hãy KHỞI ĐỘNG LẠI server để kích hoạt MegaAntiSpoof.");
    }

    private boolean allPresent() { return DEPENDENCIES.stream().allMatch(d -> present(d.pluginName)); }
    private static boolean present(String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null
                || (name.equalsIgnoreCase("packetevents") && Bukkit.getPluginManager().getPlugin("PacketEvents") != null);
    }
    private static void validateJar(Path file, long minimumBytes) throws IOException {
        if (Files.size(file) < minimumBytes) throw new IOException("tệp tải về nhỏ bất thường");
        try (InputStream input = Files.newInputStream(file)) {
            if (input.read() != 'P' || input.read() != 'K') throw new IOException("tệp tải về không phải JAR/ZIP");
        }
    }
    private static String sha256(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(file)) { input.transferTo(new DigestOutputStream(OutputStream.nullOutputStream(), digest)); }
        return HexFormat.of().formatHex(digest.digest());
    }
    private record Dependency(String pluginName, String fileName, String url, long minimumBytes) { }
}
