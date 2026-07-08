package com.lwidev.survisland.menu.widget;

import com.lwidev.survisland.api.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Modal single-line text input backed by a vanilla anvil GUI (the rename field), for the rare
 * cases where a menu needs free text — a custom announcement, a one-off timer name/duration —
 * instead of a preset button.
 *
 * <p>{@code Bukkit.createInventory(null, InventoryType.ANVIL)} only produces a static inventory
 * shell: it's never wired up as the player's actual active container, so the client's rename
 * packet has nowhere to go and {@code PrepareAnvilEvent} never fires. A real anvil GUI needs a
 * live NMS {@link AnvilMenu} set as the player's {@code containerMenu} (same trick used by the
 * AnvilGUI library, reimplemented here directly since this plugin only targets one Paper/NMS
 * version and doesn't need its cross-version abstraction layer).
 */
public final class AnvilInputMenu {

    private static Listener listener;
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private AnvilInputMenu() {
    }

    public static void open(JavaPlugin plugin, Player player, String title, String placeholder, Consumer<String> onSubmit) {
        register(plugin);

        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.network.chat.Component nmsTitle = net.minecraft.network.chat.Component.literal(title);
        int containerId = nmsPlayer.nextContainerCounter();

        AnvilContainer container = new AnvilContainer(nmsPlayer, containerId, nmsTitle);
        Inventory inventory = container.getBukkitView().getTopInventory();
        inventory.setItem(0, placeholderItem(placeholder));

        CraftEventFactory.handleInventoryCloseEvent(nmsPlayer, InventoryCloseEvent.Reason.OPEN_NEW);
        nmsPlayer.containerMenu = container;
        nmsPlayer.connection.send(new ClientboundOpenScreenPacket(containerId, MenuType.ANVIL, nmsTitle));
        nmsPlayer.initMenu(container);

        SESSIONS.put(player.getUniqueId(), new Session(inventory, container, onSubmit));
    }

    private static ItemStack placeholderItem(String placeholder) {
        return ItemBuilder.of(Material.PAPER).setName(Component.text(placeholder)).build();
    }

    private static void closeMenu(Player player) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        CraftEventFactory.handleInventoryCloseEvent(nmsPlayer, InventoryCloseEvent.Reason.PLUGIN);
        nmsPlayer.containerMenu = nmsPlayer.inventoryMenu;
    }

    private static void register(JavaPlugin plugin) {
        if (listener != null) {
            return;
        }
        listener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (!(event.getWhoClicked() instanceof Player player)) {
                    return;
                }
                Session session = SESSIONS.get(player.getUniqueId());
                if (session == null || !session.inventory.equals(event.getClickedInventory())) {
                    return;
                }
                event.setCancelled(true);
                if (event.getSlot() != 2) {
                    return;
                }
                String text = session.container.itemName;
                if (text == null || text.isBlank()) {
                    return;
                }
                SESSIONS.remove(player.getUniqueId());
                closeMenu(player);
                session.onSubmit.accept(text);
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (!(event.getPlayer() instanceof Player player)) {
                    return;
                }
                Session session = SESSIONS.get(player.getUniqueId());
                if (session != null && session.inventory.equals(event.getInventory())) {
                    SESSIONS.remove(player.getUniqueId());
                }
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                SESSIONS.remove(event.getPlayer().getUniqueId());
            }
        };
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    private static final class Session {
        private final Inventory inventory;
        private final AnvilContainer container;
        private final Consumer<String> onSubmit;

        Session(Inventory inventory, AnvilContainer container, Consumer<String> onSubmit) {
            this.inventory = inventory;
            this.container = container;
            this.onSubmit = onSubmit;
        }
    }

    /**
     * Real anvil menu (not the static Bukkit inventory shell) so the client's rename packet
     * actually reaches {@link AnvilMenu#setItemName}, which vanilla routes into {@code itemName}.
     * {@code createResult} is simplified to just mirror the input into the output slot at zero
     * XP cost — we don't need vanilla's repair/enchant-merging math for a plain text prompt, and
     * forcing cost 0 means players don't need spare XP levels to submit.
     */
    private static final class AnvilContainer extends AnvilMenu {
        AnvilContainer(ServerPlayer player, int containerId, net.minecraft.network.chat.Component title) {
            super(containerId, player.getInventory(), ContainerLevelAccess.NULL);
            this.checkReachable = false;
            setTitle(title);
        }

        @Override
        public void createResult() {
            Slot output = this.getSlot(2);
            if (!output.hasItem()) {
                output.set(this.getSlot(0).getItem().copy());
            }
            this.cost.set(0);
            this.sendAllDataToRemote();
            this.broadcastChanges();
        }

        @Override
        public void removed(net.minecraft.world.entity.player.Player player) {
            // No-op: this menu isn't backed by a real block, so vanilla's item-drop-back logic
            // (which would otherwise fire here on close) has nothing sensible to drop into.
        }

        @Override
        protected void clearContainer(net.minecraft.world.entity.player.Player player, Container container) {
            // No-op, for the same reason as removed() above.
        }
    }
}
