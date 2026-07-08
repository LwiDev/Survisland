package com.lwidev.survisland.menu.game;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.menu.widget.AnvilInputMenu;
import com.lwidev.survisland.api.menu.MenuTheme;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.PluralUtils;
import com.lwidev.survisland.menu.MenuContext;
import com.lwidev.survisland.menu.game.announce.AnnounceMenu;
import com.lwidev.survisland.utils.PauseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Session-wide controls: announcements, time of day, timers, pause, vote notifications. */
public class GameMenu extends SurvislandMenu {

    public GameMenu(Player viewer, MenuContext ctx) {
        super(viewer, 4, "Gestion de la partie");
        item(2, 4, ItemBuilder.of(Material.PAPER).setName(Component.text("Annonces", BrandUtils.SECONDARY)).build(), _ -> openSubMenu(new AnnounceMenu(viewer, ctx)));
        item(2, 5, ItemBuilder.of(Material.GRAY_DYE).setName(Component.text("Chat", NamedTextColor.DARK_GRAY)).addLore(Component.text("À venir", NamedTextColor.GRAY)).build());
        item(2, 6, ItemBuilder.of(Material.CLOCK).setName(Component.text("Temps de jeu", BrandUtils.SECONDARY)).build(), _ -> openSubMenu(new TimeMenu(viewer)));
        item(3, 4, ItemBuilder.of(Material.BELL).setName(Component.text("Timers", BrandUtils.SECONDARY)).addClickLore("Lancer un timer").build(),
                _ -> AnvilInputMenu.open(ctx.plugin(), viewer, "Nom de l'épreuve", "Course de bateaux", name -> new TimerDurationMenu(viewer, ctx, name).open()));
        Consumer<InventoryClickEvent> pauseToggleHandler = new Consumer<>() {
            @Override
            public void accept(InventoryClickEvent event) {
                boolean paused = PauseManager.toggle(ctx.plugin());
                item(3, 5, MenuTheme.toggleItem("Pause", paused), this);
                MessageUtils.sendSuccessMessage(viewer, "Partie ", MessageUtils.highlight(paused ? "mise en pause" : "reprise", BrandUtils.SECONDARY), ".");
            }
        };
        item(3, 5, MenuTheme.toggleItem("Pause", PauseManager.isPaused()), pauseToggleHandler);
        item(3, 6, ItemBuilder.of(Material.GOLD_NUGGET).setName(Component.text("Votes Gold / Marcus", BrandUtils.SECONDARY)).addClickLore("Notifier").build(), _ -> notifyVacancy(viewer, ctx));
    }

    /** Notifies GoldVision98 and Marcouscous at once — a spot freeing up concerns both, there's nothing to pick between. */
    private void notifyVacancy(Player viewer, MenuContext ctx) {
        List<String> notified = new ArrayList<>();
        List<String> unreachable = new ArrayList<>();
        for (String key : List.of("gold", "marcus")) {
            ctx.voteService().recipientName(key).ifPresentOrElse(name -> {
                if (ctx.voteService().notifyVacancy(name)) {
                    notified.add(name);
                } else {
                    unreachable.add(name + " (hors ligne)");
                }
            }, () -> unreachable.add(key + " (non configuré)"));
        }
        if (!notified.isEmpty()) {
            MessageUtils.sendSuccessMessage(viewer, MessageUtils.highlight(String.join(", ", notified), BrandUtils.PRIMARY), " ", PluralUtils.agree(notified.size(), "notifié"), " qu'une place est libre.");
        }
        if (!unreachable.isEmpty()) {
            MessageUtils.sendErrorMessage(viewer, "Non ", PluralUtils.agree(unreachable.size(), "notifié"), " : " + String.join(", ", unreachable));
        }
    }
}
