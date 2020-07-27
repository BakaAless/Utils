import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class InventoryGUI implements Listener {

    private JavaPlugin plugin;
    private Inventory inventory;
    private Map<Integer, ItemStack> slots;
    private Map<Integer, Consumer<InventoryClickEvent>> actions;
    private Map<Integer, Boolean> cancelled;

    private InventoryGUI(final JavaPlugin plugin, final int rows, final String title) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 9 * rows, title);
        this.slots = new HashMap<>();
        this.actions = new HashMap<>();
        this.cancelled = new HashMap<>();
    }

    public static InventoryGUI build(final JavaPlugin plugin, final int rows) {
        return build(plugin, rows, "");
    }

    public static InventoryGUI build(final JavaPlugin plugin, final int rows, final String title) {
        return new InventoryGUI(plugin, rows, title);
    }

    public InventoryGUI addItem(final int slot, final ItemStack itemStack) {
        return this.addItem(slot, itemStack, event -> {}, true);
    }

    public InventoryGUI addItem(final int slot, final ItemStack itemStack, final boolean cancelled) {
        return this.addItem(slot, itemStack, event -> {}, cancelled);
    }

    public InventoryGUI addItem(final int slot, final ItemStack itemStack, final Consumer<InventoryClickEvent> eventConsumer) {
        return this.addItem(slot, itemStack, eventConsumer, true);
    }

    public InventoryGUI addItem(final int slot, final ItemStack itemStack, final Consumer<InventoryClickEvent> eventConsumer, final boolean cancelled) {
        if (this.slots.containsKey(slot)) {
            this.slots.remove(slot);
            this.cancelled.remove(slot);
            this.actions.remove(slot);
        }
        if (itemStack.getType().equals(Material.AIR)) return this;
        this.slots.put(slot, itemStack);
        this.cancelled.put(slot, cancelled);
        this.actions.put(slot, eventConsumer);
        return this;
    }
    
    public ItemStack getItemStack(final int slot) {
        return this.slots.get(slot);
    }

    public void create(final Player player) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().equals(inventory)) return;
        if (e.getCurrentItem() == null) return;
        if (this.slots.containsKey(e.getSlot())) {
            e.setCancelled(this.cancelled.get(e.getSlot()));
            this.actions.get(e.getSlot()).accept(e);
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        HandlerList.unregisterAll(this);
        this.plugin = null;
        this.inventory = null;
        this.slots = null;
        this.actions = null;
        this.cancelled = null;
        try {
            this.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
}
