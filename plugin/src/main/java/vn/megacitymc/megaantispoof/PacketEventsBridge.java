package vn.megacitymc.megaantispoof;

import org.bukkit.Bukkit;

/** PacketEvents is used as the protocol/version authority; ProtocolLib owns the sign transaction. */
final class PacketEventsBridge {
    private final MegaAntiSpoofPlugin plugin;
    private final ChallengeService service;
    PacketEventsBridge(MegaAntiSpoofPlugin plugin, ChallengeService service) { this.plugin = plugin; this.service = service; }
    void enable() {
        if (Bukkit.getPluginManager().getPlugin("packetevents") == null
                && Bukkit.getPluginManager().getPlugin("PacketEvents") == null) {
            plugin.getLogger().warning("PacketEvents chưa được cài: vẫn chặn bằng ProtocolLib nhưng không có version bridge nâng cao.");
            return;
        }
        try {
            Class<?> pe = Class.forName("com.github.retrooper.packetevents.PacketEvents");
            Object api = pe.getMethod("getAPI").invoke(null);
            plugin.getLogger().info("Đã liên kết PacketEvents: " + api.getClass().getSimpleName());
            service.setPacketEventsAvailable(true);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Không thể liên kết PacketEvents: " + ex.getMessage());
        }
    }
}
