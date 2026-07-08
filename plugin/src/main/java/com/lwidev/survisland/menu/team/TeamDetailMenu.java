package com.lwidev.survisland.menu.team;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.menu.widget.ConfirmationMenu;
import com.lwidev.survisland.api.menu.widget.GameModeSelectMenu;
import com.lwidev.survisland.api.menu.player.ProfileSelectionMenu;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.PluralUtils;
import com.lwidev.survisland.menu.MenuContext;
import com.lwidev.survisland.menu.player.PlayerActionButtons;
import com.lwidev.survisland.menu.player.PlayerDetailMenu;
import com.lwidev.survisland.teams.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Objects;

/** Members of a single team, plus the team-wide actions (gamemode, give item). */
public class TeamDetailMenu extends ProfileSelectionMenu {

    public TeamDetailMenu(Player viewer, MenuContext ctx, Team team) {
        super(viewer, 6, team.getName());

        List<OfflinePlayer> members = ctx.teamManager().membersOf(team);
        selectFrom(members, (_, member) -> openSubMenu(new PlayerDetailMenu(viewer, ctx, member)));

        TextColor color = team.hasColor() ? team.color() : NamedTextColor.WHITE;
        item(1, 5, ItemBuilder.of(TeamManager.bannerMaterial(color))
                .setName(Component.text(team.getName(), color))
                .setLore(Component.text(PluralUtils.withCount(members.size(), "membre"), NamedTextColor.GRAY))
                .build());

        item(6, 4, PlayerActionButtons.gamemodeIcon("Gamemode de l'équipe"),
                _ -> openSubMenu(new GameModeSelectMenu(viewer, Component.text("Gamemode — " + team.getName()), onlineMembers(members), () -> new TeamDetailMenu(viewer, ctx, team).open())));
        item(6, 6, PlayerActionButtons.giveHandItemIcon(), _ -> giveHandItem(viewer, team, members));
    }

    private void giveHandItem(Player viewer, Team team, List<OfflinePlayer> members) {
        List<Player> online = onlineMembers(members);
        Component recipient = Component.text("à ")
                .append(MessageUtils.highlight(PluralUtils.withCount(online.size(), "membre"), BrandUtils.SECONDARY))
                .append(Component.text(" de "))
                .append(MessageUtils.highlight(team.getName(), BrandUtils.PRIMARY))
                .append(Component.text("."));
        ConfirmationMenu confirmation = PlayerActionButtons.giveHandItem(viewer, online, Component.text("Donner à " + team.getName() + " ?"), recipient);
        if (confirmation != null) {
            openSubMenu(confirmation);
        }
    }

    private List<Player> onlineMembers(List<OfflinePlayer> members) {
        return members.stream().map(OfflinePlayer::getPlayer).filter(Objects::nonNull).toList();
    }
}
