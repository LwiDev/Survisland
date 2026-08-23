package com.lwidev.survisland.services;

import com.lwidev.survisland.api.utils.Shutdownable;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/** Tracks whether players are currently made invulnerable via {@code /degats off}. */
public class DamageManager implements Listener, Shutdownable {

    private boolean invulnerable = false;

    /** @param enabled {@code true} = dégâts activés (joueurs vulnérables), {@code false} = dégâts désactivés */
    public void setEnabled(boolean enabled) {
        invulnerable = !enabled;
        Bukkit.getOnlinePlayers().forEach(player -> player.setInvulnerable(invulnerable));
    }

    public boolean isEnabled() {
        return !invulnerable;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setInvulnerable(invulnerable);
    }

    @Override
    public void shutdown() {
        if (invulnerable) {
            Bukkit.getOnlinePlayers().forEach(player -> player.setInvulnerable(false));
        }
        invulnerable = false;
    }
}
