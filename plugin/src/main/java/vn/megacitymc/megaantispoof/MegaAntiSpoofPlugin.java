package vn.megacitymc.megaantispoof;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import vn.megacitymc.megaantispoof.api.VersionAdapter;
import vn.megacitymc.megaantispoof.nms.v1_21.Adapter121;
import vn.megacitymc.megaantispoof.nms.v1_21_9.Adapter1219;
import vn.megacitymc.megaantispoof.nms.v26.Adapter26;

import java.util.List;

public final class MegaAntiSpoofPlugin extends JavaPlugin {
    private ChallengeService challenges;

    @Override public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        if (!new DependencyBootstrap(this).ensureInstalled()) {
            getLogger().warning("MegaAntiSpoof tạm vô hiệu hóa cho đến lần khởi động server tiếp theo.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        String version = Bukkit.getMinecraftVersion();
        VersionAdapter adapter = List.<VersionAdapter>of(new Adapter121(), new Adapter1219(), new Adapter26()).stream()
                .filter(a -> a.supports(version)).findFirst().orElse(null);
        if (adapter == null) {
            getLogger().severe("Phiên bản Minecraft không được hỗ trợ: " + version);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        var messages = new Messages(this);
        challenges = new ChallengeService(this, adapter, messages);
        challenges.enable();
        var command = new AntiSpoofCommand(this, challenges, messages);
        PluginCommand root = getCommand("megaantispoof");
        if (root != null) { root.setExecutor(command); root.setTabCompleter(command); }
        Bukkit.getPluginManager().registerEvents(challenges, this);
        new PacketEventsBridge(this, challenges).enable();
        getLogger().info("MegaAntiSpoof " + getDescription().getVersion() + " sẵn sàng (" + adapter.family() + ").");
    }

    @Override public void onDisable() { if (challenges != null) challenges.disable(); }
}
