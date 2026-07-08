package com.lwidev.survisland.menu.player;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.menu.widget.ConfirmationMenu;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.HeadsUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;

/**
 * Icons and actions shared by every menu that targets one or more players — a single player
 * ({@link PlayerDetailMenu}), a roster ({@link PlayerListMenu}) or a team's members
 * ({@link com.lwidev.survisland.menu.team.TeamDetailMenu}) — so the icon/name/logic trio isn't
 * hand-rolled at each call site.
 */
public final class PlayerActionButtons {

    private PlayerActionButtons() {
    }

    public static ItemStack giveHandItemIcon() {
        return ItemBuilder.skull(HeadsUtils.PRESENT).setName(Component.text("Donner l'item en main", BrandUtils.TERTIARY)).build();
    }

    public static ItemStack clearEffectsIcon() {
        return ItemBuilder.skull(HeadsUtils.EMPTY_POTION).setName(Component.text("Retirer tous les effets", BrandUtils.TERTIARY)).build();
    }

    public static ItemStack gamemodeIcon(String label) {
        return ItemBuilder.of(Material.COMMAND_BLOCK).setName(Component.text(label, BrandUtils.TERTIARY)).build();
    }

    /**
     * Builds the confirmation submenu for giving the viewer's held item to {@code targets}, to be
     * opened with {@code openSubMenu(...)} by the caller. Sends an error and returns {@code null}
     * instead if the viewer's hand is empty.
     *
     * @param recipient the recipient clause shared by both the error and success messages, e.g.
     *                  {@code Component.text("à ").append(highlight(target.getName(), BrandUtils.PRIMARY)).append(Component.text("."))}
     */
    public static ConfirmationMenu giveHandItem(Player viewer, List<Player> targets, Component confirmPrompt, Component recipient) {
        ItemStack handItem = viewer.getInventory().getItemInMainHand();
        if (handItem.getType().isAir()) {
            MessageUtils.sendErrorMessage(viewer, "Tenez un item en main pour l'offrir ", recipient);
            return null;
        }
        return new ConfirmationMenu(viewer, Component.text("Confirmer"), handItem, confirmPrompt, () -> {
            targets.forEach(target -> target.getInventory().addItem(handItem.clone()));
            MessageUtils.sendSuccessMessage(viewer, "Vous avez donné l'item ", recipient);
        });
    }

    /**
     * Clears every active potion effect from {@code targets} and reports how many were removed.
     *
     * @param noEffectsMessage sent instead if nothing was cleared, e.g. {@code "Aucun joueur n'a d'effet actif."}
     * @param summaryRecipient appended after "Vous avez retiré les effets", e.g.
     *                         {@code Component.text(" de ").append(highlight(target.getName(), BrandUtils.PRIMARY)).append(Component.text("."))}
     */
    public static void clearEffects(Player viewer, List<Player> targets, String noEffectsMessage, Component summaryRecipient) {
        int cleared = 0;
        for (Player target : targets) {
            List<PotionEffect> effects = List.copyOf(target.getActivePotionEffects());
            effects.forEach(effect -> target.removePotionEffect(effect.getType()));
            cleared += effects.size();
        }
        if (cleared == 0) {
            MessageUtils.sendErrorMessage(viewer, noEffectsMessage);
            return;
        }
        MessageUtils.sendSuccessMessage(viewer, "Vous avez retiré les effets", summaryRecipient);
    }
}
