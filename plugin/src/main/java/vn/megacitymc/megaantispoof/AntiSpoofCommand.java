package vn.megacitymc.megaantispoof;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

final class AntiSpoofCommand implements TabExecutor {
    private final MegaAntiSpoofPlugin plugin; private final ChallengeService service; private final Messages messages;
    AntiSpoofCommand(MegaAntiSpoofPlugin plugin, ChallengeService service, Messages messages) {
        this.plugin = plugin; this.service = service; this.messages = messages;
    }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("megaantispoof.admin")) { sender.sendMessage(messages.get("khong-co-quyen")); return true; }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig(); messages.reload(); service.reload(); sender.sendMessage(messages.get("da-tai-lai")); return true;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("debug")) {
            if (args.length == 1 || args[1].equalsIgnoreCase("status")) {
                boolean enabled = plugin.getConfig().getBoolean("debug.sign-response",
                        plugin.getConfig().getBoolean("debug.phan-hoi-sign", true));
                sender.sendMessage(messages.format("debug.trang-thai", "{status}", enabled ? "BẬT" : "TẮT"));
                return true;
            }
            boolean enabled;
            if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("bat")) enabled = true;
            else if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("tat")) enabled = false;
            else { sender.sendMessage(messages.get("debug.cu-phap")); return true; }
            plugin.getConfig().set("debug.sign-response", enabled);
            plugin.saveConfig();
            sender.sendMessage(messages.get(enabled ? "debug.da-bat" : "debug.da-tat"));
            return true;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("check")) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) { sender.sendMessage(messages.get("khong-tim-thay")); return true; }
            Collection<String> filter = args.length >= 3 ? Arrays.asList(args[2].toLowerCase(Locale.ROOT).split(",")) : null;
            sender.sendMessage(service.scan(target, filter) ? messages.format("bat-dau", "{player}", target.getName()) : messages.get("dang-kiem-tra"));
            return true;
        }
        sender.sendMessage(messages.get("tro-giup")); return true;
    }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("check", "debug", "reload");
        if (args.length == 2 && args[0].equalsIgnoreCase("check")) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) return List.of("on", "off", "status");
        return List.of();
    }
}
