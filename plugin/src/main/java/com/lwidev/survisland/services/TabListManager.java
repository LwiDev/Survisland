package com.lwidev.survisland.services;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.utils.Shutdownable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

public class TabListManager implements Shutdownable {

    private final Survisland plugin;
    private final AfkManager afkManager;
    private BukkitTask refreshTask;

    public TabListManager(Survisland plugin, AfkManager afkManager) {
        this.plugin = plugin;
        this.afkManager = afkManager;
        launchRefreshLoop();
    }

    @Override
    public void shutdown() {
        if (refreshTask != null) {
            refreshTask.cancel();
        }
    }

    /**
     * Force la mise à jour immédiate du tab list name d'un joueur. À appeler après une action
     * qui doit être visible instantanément (ex : depuis le menu) ; sinon la boucle périodique
     * s'en charge en moins d'une seconde.
     * @param player joueur concerné
     */
    public void refresh(Player player) {
        player.playerListName(afkManager.isAfk(player) ? Component.text("💤  ").append(buildTeamAwareName(player, NamedTextColor.GRAY)) : buildTeamAwareName(player, NamedTextColor.WHITE));
    }

    private Component buildTeamAwareName(Player player, NamedTextColor fallbackColor) {
        Team team = player.getScoreboard().getEntryTeam(player.getName());
        if (team == null) {
            return Component.text(player.getName());
        }
        return Component.empty()
                .append(team.prefix())
                .append(Component.text(player.getName()).color(team.hasColor() ? team.color() : fallbackColor))
                .append(team.suffix());
    }

    private void launchRefreshLoop() {
        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> Bukkit.getOnlinePlayers().forEach(this::refresh), 0L, 20L); // ~1s
    }
}