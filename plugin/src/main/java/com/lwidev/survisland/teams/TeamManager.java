package com.lwidev.survisland.teams;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Thin wrapper around the server's main {@link Scoreboard} teams — membership, color and
 * nametag prefix are handled natively by Bukkit/persisted by the server, so this class only
 * adds the lookups the /menu team pages need (no separate storage of its own).
 */
public class TeamManager {

    /** Colored banner closest to each named text color, for team icons in the /menu GUI. */
    private static final Map<NamedTextColor, Material> BANNER_BY_COLOR = Map.ofEntries(
            Map.entry(NamedTextColor.BLACK, Material.BLACK_BANNER),
            Map.entry(NamedTextColor.DARK_BLUE, Material.BLUE_BANNER),
            Map.entry(NamedTextColor.DARK_GREEN, Material.GREEN_BANNER),
            Map.entry(NamedTextColor.DARK_AQUA, Material.CYAN_BANNER),
            Map.entry(NamedTextColor.DARK_RED, Material.RED_BANNER),
            Map.entry(NamedTextColor.DARK_PURPLE, Material.PURPLE_BANNER),
            Map.entry(NamedTextColor.GOLD, Material.ORANGE_BANNER),
            Map.entry(NamedTextColor.GRAY, Material.LIGHT_GRAY_BANNER),
            Map.entry(NamedTextColor.DARK_GRAY, Material.GRAY_BANNER),
            Map.entry(NamedTextColor.BLUE, Material.LIGHT_BLUE_BANNER),
            Map.entry(NamedTextColor.GREEN, Material.LIME_BANNER),
            Map.entry(NamedTextColor.AQUA, Material.CYAN_BANNER),
            Map.entry(NamedTextColor.RED, Material.RED_BANNER),
            Map.entry(NamedTextColor.LIGHT_PURPLE, Material.MAGENTA_BANNER),
            Map.entry(NamedTextColor.YELLOW, Material.YELLOW_BANNER),
            Map.entry(NamedTextColor.WHITE, Material.WHITE_BANNER));

    private final Scoreboard scoreboard;

    public TeamManager() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public Collection<Team> teams() {
        return scoreboard.getTeams();
    }

    public Team createTeam(String name, NamedTextColor color) {
        Team existing = scoreboard.getTeam(name);
        if (existing != null) {
            return existing;
        }
        Team team = scoreboard.registerNewTeam(name);
        team.color(color);
        team.prefix(Component.text("[" + name + "] ").color(color));
        return team;
    }

    public void deleteTeam(Team team) {
        team.unregister();
    }

    public void addMember(Team team, OfflinePlayer player) {
        String name = player.getName();
        if (name != null) {
            team.addEntry(name);
        }
    }

    public void removeMember(Team team, OfflinePlayer player) {
        String name = player.getName();
        if (name != null) {
            team.removeEntry(name);
        }
    }

    public Optional<Team> teamOf(OfflinePlayer player) {
        String name = player.getName();
        if (name == null) {
            return Optional.empty();
        }
        return teams().stream().filter(team -> team.hasEntry(name)).findFirst();
    }

    public List<OfflinePlayer> membersOf(Team team) {
        return team.getEntries().stream().map(Bukkit::getOfflinePlayer).toList();
    }

    /** Colored banner material matching a team's color (falls back to white for teams without one). */
    public static Material bannerMaterial(TextColor color) {
        return BANNER_BY_COLOR.getOrDefault(NamedTextColor.nearestTo(color), Material.WHITE_BANNER);
    }
}
