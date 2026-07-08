package com.lwidev.survisland.menu;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.menu.MenuTheme;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.menu.game.GameMenu;
import com.lwidev.survisland.menu.player.PlayerListMenu;
import com.lwidev.survisland.menu.team.TeamListMenu;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Entry point of {@code /menu}: routes to team, player and game management. */
public class MainMenu extends SurvislandMenu {

    public MainMenu(Player viewer, MenuContext ctx) {
        super(viewer, 3, "Survisland");

        // Root menu — nothing to go back to, replace the auto-placed back button with plain filler.
        item(rows, 1, MenuTheme.filler(Material.ORANGE_STAINED_GLASS_PANE));

        item(2, 3, ItemBuilder.of(Material.WHITE_BANNER).setName(Component.text("Équipes", BrandUtils.PRIMARY)).build(), _ -> openSubMenu(new TeamListMenu(viewer, ctx)));
        item(2, 5, ItemBuilder.of(Material.PLAYER_HEAD).setName(Component.text("Joueurs", BrandUtils.PRIMARY)).build(), _ -> openSubMenu(new PlayerListMenu(viewer, ctx)));
        item(2, 7, ItemBuilder.of(Material.CLOCK).setName(Component.text("Partie", BrandUtils.PRIMARY)).build(), _ -> openSubMenu(new GameMenu(viewer, ctx)));
    }
}
