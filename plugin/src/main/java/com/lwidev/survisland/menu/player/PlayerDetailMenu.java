package com.lwidev.survisland.menu.player;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.item.SkullBuilder;
import com.lwidev.survisland.api.menu.widget.ConfirmationMenu;
import com.lwidev.survisland.api.menu.widget.GameModeSelectMenu;
import com.lwidev.survisland.api.menu.player.PlayerInventoryMenu;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.HeadsUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.menu.MenuContext;
import com.lwidev.survisland.menu.team.TeamListMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Optional;

/** Single-player actions: gamemode, inventory, effects, team, teleport. */
public class PlayerDetailMenu extends SurvislandMenu {

    public PlayerDetailMenu(Player viewer, MenuContext ctx, OfflinePlayer target) {
        super(viewer, 4, target.getName() != null ? target.getName() : "Joueur");

        item(1, 5, headItem(ctx, target), _ -> giveHead(viewer, target));

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            item(2, 3, PlayerActionButtons.giveHandItemIcon(), _ -> giveHandItem(viewer, target, onlineTarget));
            item(2, 4, ItemBuilder.of(Material.CHEST).setName(Component.text("Ouvrir l'inventaire", BrandUtils.TERTIARY)).build(), _ -> openSubMenu(new PlayerInventoryMenu(viewer, onlineTarget)));
            item(2, 6, ItemBuilder.skull(HeadsUtils.POTION).setName(Component.text("Donner saturation", BrandUtils.TERTIARY)).build(), _ -> onlineTarget.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, -1, 0)));
            item(2, 7, PlayerActionButtons.clearEffectsIcon(), _ -> clearEffects(viewer, target, onlineTarget));
            setTeamItem(ctx, onlineTarget, viewer, 3, 4);
            item(3, 5, PlayerActionButtons.gamemodeIcon("Changer gamemode"),
                    _ -> openSubMenu(new GameModeSelectMenu(viewer, Component.text("Gamemode — " + target.getName()), List.of(onlineTarget), () -> new PlayerDetailMenu(viewer, ctx, target).open())));
            item(3, 6, ItemBuilder.of(Material.ENDER_PEARL).setName(Component.text("Se téléporter", BrandUtils.TERTIARY)).build(),
                    _ -> {
                        viewer.teleport(onlineTarget.getLocation());
                        MessageUtils.sendSuccessMessage(viewer, "Téléporté vers ", MessageUtils.highlight(target.getName(), BrandUtils.PRIMARY), ".");
                    });
        } else {
            setTeamItem(ctx, target, viewer, 2, 5);
        }
    }

    private void clearEffects(Player viewer, OfflinePlayer target, Player onlineTarget) {
        Component recipient = Component.text(" de ").append(MessageUtils.highlight(target.getName(), BrandUtils.PRIMARY)).append(Component.text("."));
        PlayerActionButtons.clearEffects(viewer, List.of(onlineTarget), target.getName() + " n'a aucun effet actif.", recipient);
    }

    private void giveHandItem(Player viewer, OfflinePlayer target, Player onlineTarget) {
        Component recipient = Component.text("à ").append(MessageUtils.highlight(target.getName(), BrandUtils.PRIMARY)).append(Component.text("."));
        ConfirmationMenu confirmation = PlayerActionButtons.giveHandItem(viewer, List.of(onlineTarget),
                Component.text("Donner à " + target.getName() + " ?"), recipient);
        if (confirmation != null) {
            openSubMenu(confirmation);
        }
    }

    private void giveHead(Player viewer, OfflinePlayer target) {
        String name = target.getName() != null ? target.getName() : target.getUniqueId().toString();
        ItemStack head = ItemBuilder.skull(target.getUniqueId(), name).setName(Component.text(name, NamedTextColor.WHITE)).build();
        viewer.getInventory().addItem(head);
        MessageUtils.sendSuccessMessage(viewer, "Tête de ", MessageUtils.highlight(name, BrandUtils.PRIMARY), " obtenue.");
    }

    private ItemStack headItem(MenuContext ctx, OfflinePlayer target) {
        String name = target.getName() != null ? target.getName() : target.getUniqueId().toString();
        Optional<Team> optTeam = ctx.teamManager().teamOf(target);
        SkullBuilder skullBuilder = ItemBuilder.skull(target.getUniqueId(), name).setName(Component.text(name, NamedTextColor.WHITE).append(Component.text(" • ", NamedTextColor.GRAY)).append(state(target)));
        if (optTeam.isPresent()) {
            Team team = optTeam.get();
            skullBuilder.addLore(Component.text("Équipe : ", NamedTextColor.GRAY).append(Component.text(team.getName(), team.color())));
        } else {
            skullBuilder.addLore(Component.text("Équipe : ", NamedTextColor.GRAY).append(Component.text("Aucune", NamedTextColor.WHITE)));
        }
        return skullBuilder.addClickLore("Obtenir sa tête").build();
    }

    private void setTeamItem(MenuContext ctx, OfflinePlayer target, Player viewer, int row, int col) {
        item(row, col, ItemBuilder.of(Material.WHITE_BANNER).setName(Component.text("Changer d'équipe", BrandUtils.TERTIARY)).build(),
                _ -> openSubMenu(new TeamListMenu(viewer, ctx, team -> {
                    ctx.teamManager().addMember(team, target);
                    MessageUtils.sendSuccessMessage(viewer, MessageUtils.highlight(target.getName(), BrandUtils.PRIMARY), " rejoint ", MessageUtils.highlight(team.getName(), BrandUtils.SECONDARY), ".");
                    new PlayerDetailMenu(viewer, ctx, target).open();
                })));
    }

    private Component state(OfflinePlayer target) {
        Player online = target.getPlayer();
        if (online == null) return Component.text("Hors ligne", NamedTextColor.RED);
        return online.getGameMode() == GameMode.SPECTATOR ? Component.text("Spectateur", BrandUtils.SECONDARY) : Component.text("En ligne", NamedTextColor.GREEN);
    }
}
