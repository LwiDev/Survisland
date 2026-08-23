package com.lwidev.survisland.listeners;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Configurable join/leave chat messages, toggled and formatted via {@code join-leave} in config.yml. */
public class JoinLeaveListener implements Listener {

    private final boolean enabled;
    private final String joinFormat;
    private final String leaveFormat;

    public JoinLeaveListener(Survisland plugin) {
        this.enabled = plugin.getConfig().getBoolean("join-leave.enabled", true);
        this.joinFormat = plugin.getConfig().getString("join-leave.join-message", "&a+ &7%player% a rejoint la partie");
        this.leaveFormat = plugin.getConfig().getString("join-leave.leave-message", "&c- &7%player% a quitté la partie");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(enabled ? format(joinFormat, event.getPlayer().getName()) : null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(enabled ? format(leaveFormat, event.getPlayer().getName()) : null);
    }

    private static Component format(String format, String playerName) {
        return MessageUtils.colorize(format.replace("%player%", playerName));
    }
}
