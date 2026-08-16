package vn.megacitymc.megaantispoof;

import org.junit.jupiter.api.Test;
import vn.megacitymc.megaantispoof.api.DetectionResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WebhookClientTest {

    @Test
    void testBuildPayloadPassed() {
        WebhookClient client = new WebhookClient(null);
        UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        Instant now = Instant.parse("2026-08-16T07:30:00Z");

        List<DetectionResult.CheckedMod> checkedMods = List.of(
                new DetectionResult.CheckedMod("Meteor Client", false),
                new DetectionResult.CheckedMod("Baritone", false),
                new DetectionResult.CheckedMod("Freecam", false)
        );

        DetectionResult result = new DetectionResult(
                uuid,
                "Steve",
                "192.168.1.100",
                DetectionResult.Status.PASSED,
                "1.21.4",
                List.of(),
                List.of(),
                checkedMods,
                now
        );

        String json = client.buildPayload(result, "MegaCity #1");

        assertTrue(json.contains("\"title\":\"Kết quả kiểm tra bảo mật\""));
        assertTrue(json.contains("\"color\":5763719"));
        assertTrue(json.contains("{\"name\":\"Tài khoản\",\"value\":\"Steve\",\"inline\":true}"));
        assertTrue(json.contains("{\"name\":\"UUID\",\"value\":\"12345678-1234-1234-1234-123456789abc\",\"inline\":true}"));
        assertTrue(json.contains("{\"name\":\"IP\",\"value\":\"192.168.1.100\",\"inline\":true}"));
        assertTrue(json.contains("{\"name\":\"Máy chủ\",\"value\":\"MegaCity #1\",\"inline\":true}"));
        assertTrue(json.contains("{\"name\":\"Kết quả\",\"value\":\"PASSED\",\"inline\":true}"));
        assertTrue(json.contains("{\"name\":\"Chi tiết Quét (0/3 Phát hiện)\""));
        assertTrue(json.contains("**Meteor Client**: Không phát hiện\\n**Baritone**: Không phát hiện\\n**Freecam**: Không phát hiện"));
    }

    @Test
    void testBuildPayloadFailedWithDetections() {
        WebhookClient client = new WebhookClient(null);
        UUID uuid = UUID.fromString("87654321-4321-4321-4321-cba987654321");
        Instant now = Instant.parse("2026-08-16T07:30:00Z");

        List<DetectionResult.CheckedMod> checkedMods = List.of(
                new DetectionResult.CheckedMod("Meteor Client", true),
                new DetectionResult.CheckedMod("Baritone", false),
                new DetectionResult.CheckedMod("Freecam", true)
        );

        DetectionResult result = new DetectionResult(
                uuid,
                "Alex",
                "10.0.0.5",
                DetectionResult.Status.FAILED,
                "1.21.4",
                List.of("meteor-client", "freecam"),
                List.of("meteor-client", "freecam"),
                checkedMods,
                now
        );

        String json = client.buildPayload(result, "MegaCity Survival");

        assertTrue(json.contains("\"title\":\"Kết quả kiểm tra bảo mật\""));
        assertTrue(json.contains("\"color\":15548997"));
        assertTrue(json.contains("{\"name\":\"Tài khoản\",\"value\":\"Alex\",\"inline\":true}"));
        assertTrue(json.contains("{\"name\":\"UUID\",\"value\":\"87654321-4321-4321-4321-cba987654321\",\"inline\":true}"));
        assertTrue(json.contains("{\"name\":\"IP\",\"value\":\"10.0.0.5\",\"inline\":true}"));
        assertTrue(json.contains("{\"name\":\"Máy chủ\",\"value\":\"MegaCity Survival\",\"inline\":true}"));
        assertTrue(json.contains("{\"name\":\"Kết quả\",\"value\":\"FAILED\",\"inline\":true}"));
        assertTrue(json.contains("{\"name\":\"Chi tiết Quét (2/3 Phát hiện)\""));
        assertTrue(json.contains("**Meteor Client**: Phát hiện\\n**Baritone**: Không phát hiện\\n**Freecam**: Phát hiện"));
    }
}
