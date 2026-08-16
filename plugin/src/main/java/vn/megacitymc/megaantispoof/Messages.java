package vn.megacitymc.megaantispoof;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class Messages {
    private final MegaAntiSpoofPlugin plugin;
    private volatile YamlConfiguration yaml;
    Messages(MegaAntiSpoofPlugin plugin) { this.plugin = plugin; reload(); }
    void reload() {
        YamlConfiguration loaded = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        try (var input = plugin.getResource("messages.yml")) {
            if (input != null) loaded.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(input, StandardCharsets.UTF_8)));
        } catch (java.io.IOException ignored) { }
        yaml = loaded;
    }
    String get(String key) { return ChatColor.translateAlternateColorCodes('&', yaml.getString(key, "&cThiếu message: " + key)); }
    String format(String key, String... replacements) {
        String result = get(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) result = result.replace(replacements[i], replacements[i + 1]);
        return result;
    }
}
