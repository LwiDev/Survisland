package com.lwidev.survisland.api.menu;

import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.api.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base class for every Survisland menu (inventory GUI). Handles slot/click-handler
 * bookkeeping, sub-menu navigation and simple list pagination — all declared directly
 * from the constructor, the same way {@link SurvislandCommand}
 * subclasses declare their arguments.
 *
 * <pre>{@code
 * public class OnlinePlayersMenu extends PlayerSelectionMenu {
 *     public OnlinePlayersMenu(Player viewer) {
 *         super(viewer, 4, "Joueurs en ligne");
 *
 *         // Trivial handler: inline lambda is fine.
 *         // selectFrom(Bukkit.getOnlinePlayers().stream().toList(), (event, target) -> target.sendMessage("Sélectionné !"));
 *
 *         // Handler with real logic: extract it to a method reference to keep the constructor readable.
 *         selectFrom(Bukkit.getOnlinePlayers().stream().toList(), this::onPlayerSelected);
 *     }
 *
 *     private void onPlayerSelected(InventoryClickEvent event, Player target) {
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * <p>Rows/columns are 1-indexed, matching how a player reads a chest inventory.
 */
public abstract class SurvislandMenu {

    protected final Player player;
    protected final int rows;
    private final Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();
    private final Set<Integer> editableSlots = new HashSet<>();

    protected SurvislandMenu(Player player, int rows, String title) {
        this(player, rows, MenuTheme.formatTitle(title));
    }

    protected SurvislandMenu(Player player, int rows, Component title) {
        this.player = player;
        this.rows = rows;
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        MenuTheme.decorateCorners(this, rows);
        MenuTheme.addBackButton(this, rows);
    }

    /** Places an item with no click behavior. */
    protected final void item(int row, int col, ItemStack stack) {
        inventory.setItem(slot(row, col), stack);
    }

    /** Places an item and binds a handler to its slot. */
    protected final void item(int row, int col, ItemStack stack, Consumer<InventoryClickEvent> onClick) {
        int slot = slot(row, col);
        inventory.setItem(slot, stack);
        clickHandlers.put(slot, onClick);
    }

    protected final void removeItem(int row, int col) {
        int slot = slot(row, col);
        inventory.setItem(slot, null);
        clickHandlers.remove(slot);
    }

    /** Reads back whatever currently sits in that slot (e.g. after a player edited an {@link #markEditable} slot). */
    protected final ItemStack itemAt(int row, int col) {
        return inventory.getItem(slot(row, col));
    }

    /**
     * Marks a slot as a real, freely-editable inventory slot instead of a fixed button: clicks and
     * drags touching only editable slots go through unmodified (no cancel, no click handler) so the
     * player can pick up/place/swap items normally. See {@link com.lwidev.survisland.api.menu.player.PlayerInventoryMenu}
     * for the intended use case (mirroring another player's live inventory).
     */
    protected final void markEditable(int row, int col) {
        editableSlots.add(slot(row, col));
    }

    /** Same as {@link #markEditable(int, int)}, for a whole rectangular block at once (rows and columns inclusive). */
    protected final void markEditable(int fromRow, int fromCol, int toRow, int toCol) {
        for (int row = fromRow; row <= toRow; row++) {
            for (int col = fromCol; col <= toCol; col++) {
                markEditable(row, col);
            }
        }
    }

    /**
     * Called once (already scheduled for the next tick, after Bukkit applies the change) whenever a
     * click or drag touching only {@link #markEditable} slots goes through. No-op by default.
     */
    protected void onEditableSlotChanged() {
    }

    private int slot(int row, int col) {
        return (row - 1) * 9 + (col - 1);
    }

    /**
     * Declares a paginated list in a centered grid — a one-column border of filler all
     * around, and the row right below the grid free for the previous/next controls (and
     * the back button, already in its bottom-left corner).
     */
    protected final <T> void paginate(List<T> items, Function<T, ItemStack> itemFactory, BiConsumer<InventoryClickEvent, T> onSelect) {
        paginate(2, 2, 7, rows - 2, items, itemFactory, onSelect);
    }

    /**
     * Declares a paginated list of items directly in the menu layout — no separate
     * pagination object to create or hold onto. Fills from (startRow, startCol),
     * wrapping every {@code itemsPerRow} columns across {@code pageRows} rows, and
     * places previous/next buttons on the row right below once the list overflows
     * a single page.
     */
    protected final <T> void paginate(int startRow, int startCol, int itemsPerRow, int pageRows, List<T> items, Function<T, ItemStack> itemFactory, BiConsumer<InventoryClickEvent, T> onSelect) {
        new Pagination<>(startRow, startCol, itemsPerRow, pageRows, items, itemFactory, onSelect).render(0);
    }

    /** Opens this menu for its player, replacing whatever menu (if any) they currently have open. */
    public final void open() {
        SurvislandMenuManager.get().open(player, this);
    }

    public final void close() {
        player.closeInventory();
    }

    /** Closes then reopens this menu for its player. */
    protected final void refresh() {
        close();
        open();
    }

    /** Opens a child menu, remembering this one so {@link #back()} can return to it. */
    protected final void openSubMenu(SurvislandMenu subMenu) {
        SurvislandMenuManager.get().openSubMenu(player, subMenu);
    }

    /** Returns to the menu that opened this one via {@link #openSubMenu}, or closes if there is none. */
    protected final void back() {
        SurvislandMenuManager.get().back(player);
    }

    final Inventory inventory() {
        return inventory;
    }

    final void handleClick(InventoryClickEvent event) {
        boolean clickedTop = inventory.equals(event.getClickedInventory());
        if (clickedTop) {
            if (editableSlots.contains(event.getSlot())) {
                return;
            }
        } else if (!editableSlots.isEmpty()) {
            // This menu is a real inventory editor (e.g. PlayerInventoryMenu): let the viewer freely
            // move items into/out of their own inventory too, not just within the editable slots above.
            return;
        }
        event.setCancelled(true);
        if (!clickedTop) {
            return;
        }
        Consumer<InventoryClickEvent> handler = clickHandlers.get(event.getSlot());
        if (handler != null) {
            player.playSound(player.getLocation(), MenuTheme.CLICK_SOUND, 0.8f, 1.0f);
            handler.accept(event);
        }
    }

    /** Used by {@link SurvislandMenuManager} to allow drags that touch only editable slots. */
    final boolean isEditableSlot(int slot) {
        return editableSlots.contains(slot);
    }

    /** Used by {@link SurvislandMenuManager} to also allow drags spilling into the viewer's own inventory. */
    final boolean hasEditableSlots() {
        return !editableSlots.isEmpty();
    }

    /** Inline pagination state — kept private so the public API surface stays a single {@code paginate(...)} call. */
    private final class Pagination<T> {
        private final int startRow;
        private final int startCol;
        private final int itemsPerRow;
        private final int pageRows;
        private final int itemsPerPage;
        private final List<T> items;
        private final Function<T, ItemStack> itemFactory;
        private final BiConsumer<InventoryClickEvent, T> onSelect;

        Pagination(int startRow, int startCol, int itemsPerRow, int pageRows, List<T> items,
                   Function<T, ItemStack> itemFactory, BiConsumer<InventoryClickEvent, T> onSelect) {
            this.startRow = startRow;
            this.startCol = startCol;
            this.itemsPerRow = itemsPerRow;
            this.pageRows = pageRows;
            this.itemsPerPage = itemsPerRow * pageRows;
            this.items = items;
            this.itemFactory = itemFactory;
            this.onSelect = onSelect;
        }

        void render(int page) {
            clearArea();
            int from = page * itemsPerPage;
            int to = Math.min(from + itemsPerPage, items.size());
            for (int i = from; i < to; i++) {
                T value = items.get(i);
                int rel = i - from;
                int row = startRow + rel / itemsPerRow;
                int col = startCol + rel % itemsPerRow;
                item(row, col, itemFactory.apply(value), event -> onSelect.accept(event, value));
            }
            renderControls(page);
        }

        void clearArea() {
            for (int r = 0; r < pageRows; r++) {
                for (int c = 0; c < itemsPerRow; c++) {
                    removeItem(startRow + r, startCol + c);
                }
            }
        }

        void renderControls(int page) {
            int controlsRow = startRow + pageRows;
            int center = startCol + itemsPerRow / 2;
            int prevCol = center - 1;
            int nextCol = center + 1;
            int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));
            if (totalPages <= 1) {
                return;
            }
            if (page > 0) {
                item(controlsRow, prevCol, MenuTheme.previousPageItem(), _ -> render(page - 1));
            } else {
                removeItem(controlsRow, prevCol);
            }
            if (page < totalPages - 1) {
                item(controlsRow, nextCol, MenuTheme.nextPageItem(), _ -> render(page + 1));
            } else {
                removeItem(controlsRow, nextCol);
            }
        }
    }
}
