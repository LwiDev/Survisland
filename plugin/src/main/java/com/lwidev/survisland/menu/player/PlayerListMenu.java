package com.lwidev.survisland.menu.player;

import com.lwidev.survisland.api.menu.player.ProfileSelectionMenu;
import com.lwidev.survisland.api.menu.widget.ConfirmationMenu;
import com.lwidev.survisland.api.menu.widget.GameModeSelectMenu;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.PluralUtils;
import com.lwidev.survisland.menu.MenuContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Every online player, plus offline players already on a team (a full server roster of
 * "anyone who ever joined" would be unusable in a paginated GUI). The last row holds
 * global actions applied to every listed player, mirroring the team-wide actions in
 * {@link com.lwidev.survisland.menu.team.TeamDetailMenu}.
 */
public class PlayerListMenu extends ProfileSelectionMenu {

    public PlayerListMenu(Player viewer, MenuContext ctx) {
        super(viewer, 6, "Joueurs");

        List<OfflinePlayer> candidates = new ArrayList<>(Bukkit.getOnlinePlayers());
        ctx.teamManager().teams().forEach(team -> ctx.teamManager().membersOf(team).forEach(member -> {
            if (!member.isOnline() && !candidates.contains(member)) {
                candidates.add(member);
            }
        }));

        selectFrom(2, 2, 7, 4, candidates, (_, target) -> openSubMenu(new PlayerDetailMenu(viewer, ctx, target)));

        List<Player> onlinePlayers = onlinePlayers(candidates);
        item(6, 4, PlayerActionButtons.clearEffectsIcon(), _ -> clearAllEffects(viewer, onlinePlayers));
        item(6, 5, PlayerActionButtons.giveHandItemIcon(), _ -> giveHandItem(viewer, onlinePlayers));
        item(6, 6, PlayerActionButtons.gamemodeIcon("Changer gamemode"),
                _ -> openSubMenu(new GameModeSelectMenu(viewer, Component.text("Gamemode — Joueurs"), onlinePlayers, () -> new PlayerListMenu(viewer, ctx).open())));
    }

    private void clearAllEffects(Player viewer, List<Player> targets) {
        Component recipient = Component.text(" sur ").append(MessageUtils.highlight(PluralUtils.withCount(targets.size(), "joueur"), BrandUtils.SECONDARY)).append(Component.text("."));
        PlayerActionButtons.clearEffects(viewer, targets, "Aucun joueur n'a d'effet actif.", recipient);
    }

    private void giveHandItem(Player viewer, List<Player> targets) {
        Component recipient = Component.text("à ").append(MessageUtils.highlight(PluralUtils.withCount(targets.size(), "joueur"), BrandUtils.SECONDARY)).append(Component.text("."));
        ConfirmationMenu confirmation = PlayerActionButtons.giveHandItem(viewer, targets, Component.text("Donner à tous les joueurs ?"), recipient);
        if (confirmation != null) {
            openSubMenu(confirmation);
        }
    }

    private List<Player> onlinePlayers(List<OfflinePlayer> candidates) {
        return candidates.stream().map(OfflinePlayer::getPlayer).filter(Objects::nonNull).toList();
    }
}
