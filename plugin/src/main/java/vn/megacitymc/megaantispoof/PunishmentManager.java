package vn.megacitymc.megaantispoof;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class PunishmentManager {
    private final MegaAntiSpoofPlugin plugin;
    private final Messages messages;
    private final File file;
    private final Map<UUID, Integer> violations = new ConcurrentHashMap<>();

    PunishmentManager(MegaAntiSpoofPlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
        this.file = new File(plugin.getDataFolder(), "violations.yml");
        load();
    }

    void load() {
        violations.clear();
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int count = config.getInt(key, 0);
                if (count > 0) violations.put(uuid, count);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    synchronized void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Integer> entry : violations.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0) {
                config.set(entry.getKey().toString(), entry.getValue());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Không thể lưu file violations.yml: " + e.getMessage());
        }
    }

    int getViolations(UUID uuid) {
        return violations.getOrDefault(uuid, 0);
    }

    int recordViolation(UUID uuid) {
        int next = violations.merge(uuid, 1, Integer::sum);
        save();
        return next;
    }

    void resetViolations(UUID uuid) {
        if (violations.remove(uuid) != null) {
            save();
        }
    }

    boolean isBanEnabled() {
        return plugin.getConfig().getBoolean("punishment.ban-enabled", true);
    }

    int getMaxViolations() {
        return plugin.getConfig().getInt("punishment.max-violations", 3);
    }

    boolean shouldResetOnPass() {
        return plugin.getConfig().getBoolean("punishment.reset-on-pass", true);
    }

    String getDiscordInvite() {
        return plugin.getConfig().getString("punishment.discord-invite", "https://discord.gg/megacitymc");
    }

    List<String> getBanCommands() {
        return plugin.getConfig().getStringList("punishment.ban-commands");
    }

    void executeBan(Player player, String mods, int currentViolations) {
        int max = getMaxViolations();
        String discord = getDiscordInvite();
        String ip = player.getAddress() != null && player.getAddress().getAddress() != null
                ? player.getAddress().getAddress().getHostAddress()
                : "Không xác định";

        List<String> customCommands = getBanCommands();
        if (customCommands != null && !customCommands.isEmpty()) {
            for (String rawCmd : customCommands) {
                if (rawCmd == null || rawCmd.isBlank()) continue;
                String cmd = rawCmd
                        .replace("{player}", player.getName())
                        .replace("{uuid}", player.getUniqueId().toString())
                        .replace("{ip}", ip)
                        .replace("{mods}", mods)
                        .replace("{violations}", String.valueOf(currentViolations))
                        .replace("{max_violations}", String.valueOf(max))
                        .replace("{discord}", discord);
                SchedulerFacade.runGlobal(plugin, () -> {
                    try {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    } catch (Throwable t) {
                        plugin.getLogger().warning("Lỗi khi chạy lệnh ban: " + cmd + " (" + t.getMessage() + ")");
                    }
                });
            }
        } else {
            String banReason = messages.format("ban-reason",
                    "{player}", player.getName(),
                    "{mods}", mods,
                    "{violations}", String.valueOf(currentViolations),
                    "{max_violations}", String.valueOf(max),
                    "{discord}", discord);
            if (banReason.startsWith("&cThiếu message:")) {
                banReason = "Cố chấp dùng mod cấm (" + mods + ") quá " + max + " lần. Kháng cáo: " + discord;
            }

            try {
                @SuppressWarnings({"unchecked", "deprecation"})
                BanList<String> banList = (BanList<String>) Bukkit.getBanList(BanList.Type.NAME);
                banList.addBan(player.getName(), banReason, (java.time.Instant) null, "MegaAntiSpoof");
            } catch (Throwable t) {
                plugin.getLogger().warning("Không thể thêm người chơi vào BanList: " + t.getMessage());
            }
        }

        String alertText = messages.format("ket-qua.da-ban",
                "{player}", player.getName(),
                "{mods}", mods,
                "{violations}", String.valueOf(currentViolations),
                "{max_violations}", String.valueOf(max),
                "{discord}", discord);
        Bukkit.getConsoleSender().sendMessage(alertText);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("megaantispoof.alert")) {
                online.sendMessage(alertText);
            }
        }

        String banKickMessage = messages.format("ban",
                "{player}", player.getName(),
                "{mods}", mods,
                "{violations}", String.valueOf(currentViolations),
                "{max_violations}", String.valueOf(max),
                "{discord}", discord);
        if (player.isOnline()) {
            player.kickPlayer(banKickMessage);
        }
    }

    void executeKick(Player player, String mods, int currentViolations) {
        int max = getMaxViolations();
        String discord = getDiscordInvite();
        String kickMessage = messages.format("kick",
                "{player}", player.getName(),
                "{mods}", mods,
                "{violations}", String.valueOf(currentViolations),
                "{max_violations}", String.valueOf(max),
                "{discord}", discord);
        if (player.isOnline()) {
            player.kickPlayer(kickMessage);
        }
    }

    void executeNoResponseKick(Player player) {
        String discord = getDiscordInvite();
        String kickMessage = messages.format("kick-khong-phan-hoi",
                "{player}", player.getName(),
                "{discord}", discord);
        if (player.isOnline()) {
            player.kickPlayer(kickMessage);
        }
    }
}
