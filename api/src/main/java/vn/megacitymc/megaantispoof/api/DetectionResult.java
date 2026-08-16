package vn.megacitymc.megaantispoof.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DetectionResult(
        UUID playerId,
        String playerName,
        String playerIp,
        Status status,
        String clientVersion,
        List<String> detectedMods,
        List<String> reportedMods,
        List<CheckedMod> checkedMods,
        Instant timestamp
) {
    public DetectionResult {
        playerIp = (playerIp != null && !playerIp.isBlank()) ? playerIp : "Không xác định";
        detectedMods = detectedMods != null ? List.copyOf(detectedMods) : List.of();
        reportedMods = reportedMods != null ? List.copyOf(reportedMods) : List.of();
        checkedMods = checkedMods != null ? List.copyOf(checkedMods) : List.of();
        timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public DetectionResult(UUID playerId, String playerName, Status status,
                          String clientVersion, List<String> detectedMods,
                          List<String> reportedMods, Instant timestamp) {
        this(playerId, playerName, "Không xác định", status, clientVersion,
                detectedMods, reportedMods, List.of(), timestamp);
    }

    public record CheckedMod(String name, boolean detected) {}

    public enum Status { PASSED, FAILED, PROTECTED, ERROR }
}

