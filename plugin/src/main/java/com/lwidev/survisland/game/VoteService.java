package com.lwidev.survisland.game;

import com.lwidev.survisland.Survisland;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Optional;

/**
 * Notifies one of the two fixed vote recipients (configured under {@code menu.vote-recipients})
 * with an in-game title when a spot frees up — they aren't the one voting, they pass the word to
 * the next player waiting to vote.
 */
public class VoteService {

    private final Survisland plugin;

    public VoteService(Survisland plugin) {
        this.plugin = plugin;
    }

    public Optional<String> recipientName(String key) {
        String name = plugin.getConfig().getString("menu.vote-recipients." + key);
        return Optional.ofNullable(name).filter(n -> !n.isBlank());
    }

    /** @return true if the recipient was online and notified, false if they're offline. */
    public boolean notifyVacancy(String recipientName) {
        Player target = Bukkit.getPlayer(recipientName);
        if (target == null) {
            return false;
        }
        Title title = Title.title(
                Component.text("Emplacement libre !", NamedTextColor.GREEN),
                Component.empty(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500)));
        target.showTitle(title);
        return true;
    }
}
