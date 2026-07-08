package com.lwidev.survisland.menu.team;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import com.lwidev.survisland.api.utils.PluralUtils;
import com.lwidev.survisland.menu.MenuContext;
import com.lwidev.survisland.teams.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lists every team. Doubles as a team-picker: pass an {@code onPick} callback (e.g. from
 * {@code PlayerDetailMenu}'s "changer d'équipe" button) to select a team instead of opening
 * its detail page.
 */
public class TeamListMenu extends SurvislandMenu {

    public TeamListMenu(Player viewer, MenuContext ctx) {
        this(viewer, ctx, null);
    }

    public TeamListMenu(Player viewer, MenuContext ctx, Consumer<Team> onPick) {
        super(viewer, 6, "Équipes");

        List<Team> teams = new ArrayList<>(ctx.teamManager().teams());
        paginate(teams, team -> teamItem(team, onPick != null), (_, team) -> {
            if (onPick != null) {
                onPick.accept(team);
            } else {
                openSubMenu(new TeamDetailMenu(viewer, ctx, team));
            }
        });

    }

    private ItemStack teamItem(Team team, boolean picking) {
        TextColor color = team.hasColor() ? team.color() : NamedTextColor.WHITE;
        ItemBuilder builder = ItemBuilder.of(TeamManager.bannerMaterial(color))
                .setName(Component.text(team.getName(), color))
                .setLore(Component.text(PluralUtils.withCount(team.getSize(), "membre"), NamedTextColor.GRAY));
        if (picking) {
            builder.addClickLore("Choisir cette équipe");
        }
        return builder.build();
    }
}
