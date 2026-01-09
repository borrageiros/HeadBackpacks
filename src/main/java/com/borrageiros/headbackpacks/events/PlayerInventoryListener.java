package com.borrageiros.headbackpacks.events;

import com.borrageiros.headbackpacks.HeadBackpacks;
import com.borrageiros.headbackpacks.gui.BackpackGUI;
import com.borrageiros.headbackpacks.gui.PlacedBackpackGUI;
import com.borrageiros.headbackpacks.utils.items.PersistentDataUtils;
import com.borrageiros.headbackpacks.utils.items.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class PlayerInventoryListener implements Listener {
    final Plugin plugin;
    private static final int HELMET_SLOT = 39;
    private static final int CHESTPLATE_SLOT = 38;

    public PlayerInventoryListener(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private boolean isBackpack(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        return PersistentDataUtils.hasData(item, "type");
    }

    private boolean isBackpackInventory(Inventory inventory) {
        return inventory.getHolder() instanceof BackpackGUI || inventory.getHolder() instanceof PlacedBackpackGUI;
    }

    private boolean isHelmetSlot(int slot) {
        return slot == HELMET_SLOT;
    }

    private boolean isChestplateSlot(int slot) {
        return slot == CHESTPLATE_SLOT;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInventoryInteract(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursorItem = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();
        boolean cursorIsBackpack = isBackpack(cursorItem);
        boolean currentIsBackpack = isBackpack(currentItem);

        if (isHelmetSlot(event.getSlot()) && cursorIsBackpack) {
            event.setCancelled(true);
            return;
        }

        if (event.isShiftClick() && currentIsBackpack) {
            if (isBackpackInventory(event.getClickedInventory()) || isBackpackInventory(event.getInventory())) {
                event.setCancelled(true);
                return;
            }
            if (event.getClickedInventory().equals(player.getInventory())) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    ItemStack helmet = player.getInventory().getHelmet();
                    ItemStack chestplate = player.getInventory().getChestplate();

                    // If the chest slot is empty, move the backpack there
                    if (chestplate == null || chestplate.getType() == Material.AIR) {
                        player.getInventory().setChestplate(currentItem);
                        event.getClickedInventory().setItem(event.getSlot(), null);
                        player.updateInventory();
                        return;
                    }

                    // If the chest slot is occupied, do nothing and prevent placement in the helmet
                    event.setCancelled(true);
                }, 1L);
            }
        }

        if (isChestplateSlot(event.getSlot()) && cursorIsBackpack) {
            event.setCancelled(true);
            ItemStack chestplateItem = player.getInventory().getChestplate();
            player.getInventory().setChestplate(cursorItem);
            player.setItemOnCursor(chestplateItem);
            return;
        }

        if (event.getClick().isRightClick() && event.getCurrentItem() != null) {
            ItemStack clickedItem = event.getCurrentItem();
            if (!isBackpack(clickedItem)) return;

            event.setCancelled(true);
            player.setItemOnCursor(null);

            if (isBackpackInventory(event.getInventory())) {
                if (event.getInventory().getHolder() instanceof BackpackGUI currentBackpack) {
                    String contentSerialized = SerializationUtils.inventoryToBase64(currentBackpack.getInventory());
                    PersistentDataUtils.addStringData(currentBackpack.getBackpack(), "content", contentSerialized);
                }
                if (event.getInventory().getHolder() instanceof PlacedBackpackGUI placedBackpack) {
                    Location blockLocation = placedBackpack.getBlockLocation();
                    Block block = blockLocation.getBlock();
                    if (block.getState() instanceof Skull skull) {
                        String contentSerialized = SerializationUtils.inventoryToBase64(placedBackpack.getInventory());
                        PersistentDataContainer container = skull.getPersistentDataContainer();
                        container.set(new NamespacedKey(HeadBackpacks.getInstance(), "content"), PersistentDataType.STRING, contentSerialized);
                        skull.update();
                    }
                }
            }

            String serializedContent = PersistentDataUtils.getStringData(clickedItem, "content");
            Inventory inventoryContent;

            if (serializedContent == null || serializedContent.isEmpty())
                inventoryContent = Bukkit.createInventory(null, 9);
            else
                inventoryContent = SerializationUtils.inventoryFromBase64(serializedContent);

            int backpackRows = PersistentDataUtils.getIntData(clickedItem, "rows");

            ItemMeta meta = clickedItem.getItemMeta();
            String displayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "Backpack";

            player.openInventory(new BackpackGUI(displayName, backpackRows * 9, clickedItem, inventoryContent).getInventory());
        }

        // Cancel any attempt to move the backpack if it is open
        if (currentIsBackpack || cursorIsBackpack) {
            if (isBackpackInventory(event.getClickedInventory()) || isBackpackInventory(event.getInventory())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursorItem = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();

        boolean cursorIsBackpack = isBackpack(cursorItem);
        boolean currentIsBackpack = isBackpack(currentItem);

        // Prevent placing a backpack in the helmet slot
        if (isHelmetSlot(event.getSlot()) && cursorIsBackpack) {
            event.setCancelled(true);
            return;
        }

        // Allow placing a backpack in the chestplate slot (swap if necessary)
        if (isChestplateSlot(event.getSlot()) && cursorIsBackpack) {
            event.setCancelled(true);
            ItemStack chestplateItem = player.getInventory().getChestplate();
            player.getInventory().setChestplate(cursorItem);
            player.setItemOnCursor(chestplateItem);
            return;
        }

        // Prevent placing a backpack inside another backpack
        if (isBackpackInventory(event.getClickedInventory()) && cursorIsBackpack) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryShiftClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack currentItem = event.getCurrentItem();
        boolean currentIsBackpack = isBackpack(currentItem);

        if (event.isShiftClick() && currentIsBackpack) {
            // Prevent placing a backpack in the helmet slot
            if (isHelmetSlot(event.getSlot())) {
                event.setCancelled(true);
                return;
            }

            // Allow placing a backpack in the chestplate slot (swap if necessary)
            if (player.getInventory().getChestplate() == null || player.getInventory().getChestplate().getType() == Material.AIR) {
                player.getInventory().setChestplate(currentItem);
                event.getClickedInventory().setItem(event.getSlot(), null);
                player.updateInventory();
                return;
            }

            // Prevent placing a backpack in the helmet slot even if the chest is occupied
            if (player.getInventory().getHelmet() == null || player.getInventory().getHelmet().getType() == Material.AIR) {
                event.setCancelled(true);
                return;
            }

            // Prevent placing a backpack inside another backpack
            if (isBackpackInventory(event.getClickedInventory())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack draggedItem = event.getOldCursor();
        if (!isBackpack(draggedItem)) return;

        // Prevent dragging a backpack into another backpack
        if (isBackpackInventory(event.getInventory())) {
            event.setCancelled(true);
            return;
        }

        int topInventorySize = event.getInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < topInventorySize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof BackpackGUI backpack) {
            if (backpack.getBackpack() == null || backpack.getBackpack().getType() == Material.AIR) return;
            if (!backpack.getBackpack().hasItemMeta()) return;

            String contentSerialized = SerializationUtils.inventoryToBase64(backpack.getInventory());
            PersistentDataUtils.addStringData(backpack.getBackpack(), "content", contentSerialized);

            if (event.getPlayer() instanceof Player player) {
                Bukkit.getScheduler().runTaskLater(plugin, player::updateInventory, 1L);
            }
            return;
        }

        if (event.getInventory().getHolder() instanceof PlacedBackpackGUI placedBackpack) {
            Location blockLocation = placedBackpack.getBlockLocation();
            Block block = blockLocation.getBlock();

            if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return;
            if (!(block.getState() instanceof Skull skull)) return;

            String contentSerialized = SerializationUtils.inventoryToBase64(placedBackpack.getInventory());

            PersistentDataContainer container = skull.getPersistentDataContainer();
            container.set(new NamespacedKey(HeadBackpacks.getInstance(), "content"), PersistentDataType.STRING, contentSerialized);
            skull.update();
        }
    }
}
