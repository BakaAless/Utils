import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InventoryGUI implements Listener {

    private boolean created;

    private Inventory inventory;

    private JavaPlugin plugin;

    private Map<Integer, ItemStack> slots;
    private Map<Integer, Consumer<InventoryClickEvent>> actions;
    private Map<Integer, Boolean> cancelled;

    private List<Player> viewers;

    private InventoryGUI(final JavaPlugin plugin, final int rows, final String title) {
        this.created = false;
        this.inventory = Bukkit.createInventory(null, 9 * rows, ChatColor.translateAlternateColorCodes('&', title));
        this.plugin = plugin;
        this.slots = new HashMap<>();
        this.actions = new HashMap<>();
        this.cancelled = new HashMap<>();
        this.viewers = new ArrayList<>();
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
        if (this.created) return this;
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

    public InventoryGUI create() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.created = true;
        return this;
    }

    public void render(final Player player) {
        if (!this.created) return;
        player.openInventory(inventory);
        this.viewers.add(player);
    }

    public void close(final Player player) {
        if (!this.created) return;
        if (!this.viewers.contains(player)) return;
        player.closeInventory();
        this.viewers.remove(player);
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
        if (!e.getInventory().equals(inventory)) return;
        this.viewers.remove((Player) e.getPlayer());
        if (this.viewers.size() > 0) return;
        HandlerList.unregisterAll(this);
        this.plugin = null;
        this.inventory = null;
        this.slots = null;
        this.actions = null;
        this.cancelled = null;
        this.viewers = null;
        try {
            this.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
