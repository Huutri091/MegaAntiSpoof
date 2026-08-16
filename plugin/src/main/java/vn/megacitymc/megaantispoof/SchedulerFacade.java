package vn.megacitymc.megaantispoof;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.lang.reflect.Method;
import java.util.function.Consumer;

final class SchedulerFacade {
    private SchedulerFacade() { }
    static void later(MegaAntiSpoofPlugin plugin, Player player, long ticks, Runnable task) {
        try {
            Method getScheduler = player.getClass().getMethod("getScheduler");
            Object scheduler = getScheduler.invoke(player);
            Method runDelayed = scheduler.getClass().getMethod("runDelayed", org.bukkit.plugin.Plugin.class,
                    Consumer.class, Runnable.class, long.class);
            runDelayed.invoke(scheduler, plugin, (Consumer<Object>) ignored -> task.run(), null, Math.max(1, ticks));
        } catch (ReflectiveOperationException ignored) {
            Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(1, ticks));
        }
    }
}
