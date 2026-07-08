package com.lwidev.survisland.game;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.utils.Shutdownable;
import com.lwidev.survisland.api.utils.SoundUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedHashMap;
import java.util.Map;

/** Named countdown timers (e.g. "épreuve"), started/listed/cancelled from the /menu game page. */
public class TimerService implements Shutdownable {

    private final Survisland plugin;
    private final Map<String, BukkitTask> tasks = new LinkedHashMap<>();
    private final Map<String, Long> endTimesMillis = new LinkedHashMap<>();

    public TimerService(Survisland plugin) {
        this.plugin = plugin;
    }

    public void start(String name, long durationTicks) {
        cancel(name);
        endTimesMillis.put(name, System.currentTimeMillis() + durationTicks * 50L);
        SoundUtils.playNotify();
        tasks.put(name, Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcast(Component.text("[Timer] ", NamedTextColor.GOLD).append(Component.text(name + " est terminé !", NamedTextColor.YELLOW)));
            SoundUtils.playComplete();
            tasks.remove(name);
            endTimesMillis.remove(name);
        }, durationTicks));
    }

    public void cancel(String name) {
        BukkitTask task = tasks.remove(name);
        if (task != null) {
            task.cancel();
        }
        endTimesMillis.remove(name);
    }

    /** Timer name -> remaining seconds. */
    public Map<String, Long> activeTimers() {
        long now = System.currentTimeMillis();
        Map<String, Long> remaining = new LinkedHashMap<>();
        endTimesMillis.forEach((name, endTime) -> remaining.put(name, Math.max(0L, (endTime - now) / 1000L)));
        return remaining;
    }

    @Override
    public void shutdown() {
        tasks.values().forEach(BukkitTask::cancel);
        tasks.clear();
        endTimesMillis.clear();
    }
}
