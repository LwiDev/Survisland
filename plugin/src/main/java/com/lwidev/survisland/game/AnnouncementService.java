package com.lwidev.survisland.game;

import com.lwidev.survisland.Survisland;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import com.lwidev.survisland.api.utils.SoundUtils;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.util.List;

/** Immediate or delayed server-wide broadcasts, plus the configurable preset messages. */
public class AnnouncementService {

    private static final Title.Times TITLE_TIMES = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(500));

    private final Survisland plugin;

    public AnnouncementService(Survisland plugin) {
        this.plugin = plugin;
    }

    /** Preset messages configured under {@code menu.announcement-presets} in config.yml. */
    public List<String> presets() {
        return plugin.getConfig().getStringList("menu.announcement-presets");
    }

    /** Sends the message in chat and as a subtitle overlay, so it isn't missed mid-fight. */
    public void broadcastNow(String message) {
        Bukkit.broadcast(Component.text("[Annonce] ", NamedTextColor.GOLD).append(Component.text(message, NamedTextColor.YELLOW)));

        Title title = Title.title(Component.empty(), Component.text(message, NamedTextColor.YELLOW), TITLE_TIMES);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.showTitle(title);
            SoundUtils.playNotify(player);
        });
    }

    public void schedule(String message, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> broadcastNow(message), delayTicks);
    }
}
