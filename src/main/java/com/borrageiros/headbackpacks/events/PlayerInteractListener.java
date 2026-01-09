package com.borrageiros.headbackpacks.events;

import com.borrageiros.headbackpacks.HeadBackpacks;
import com.borrageiros.headbackpacks.gui.BackpackGUI;
import com.borrageiros.headbackpacks.gui.PlacedBackpackGUI;
import com.borrageiros.headbackpacks.utils.items.PersistentDataUtils;
import com.borrageiros.headbackpacks.utils.items.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class PlayerInteractListener implements Listener {
    final Plugin plugin;

    public PlayerInteractListener(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private boolean isPlayerHeadWithContent(org.bukkit.inventory.ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.PLAYER_HEAD && PersistentDataUtils.hasData(item, "content");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && (clickedBlock.getType() == Material.PLAYER_HEAD || clickedBlock.getType() == Material.PLAYER_WALL_HEAD)) {
                if (clickedBlock.getState() instanceof Skull skull) {
                    PersistentDataContainer container = skull.getPersistentDataContainer();
                    NamespacedKey typeKey = new NamespacedKey(HeadBackpacks.getInstance(), "type");

                    if (container.has(typeKey, PersistentDataType.STRING)) {
                        event.setCancelled(true);

                        String type = container.get(typeKey, PersistentDataType.STRING);
                        String content = container.get(new NamespacedKey(HeadBackpacks.getInstance(), "content"), PersistentDataType.STRING);
                        Integer rows = container.get(new NamespacedKey(HeadBackpacks.getInstance(), "rows"), PersistentDataType.INTEGER);

                        Inventory inventoryContent;
                        if (content == null || content.isEmpty()) {
                            inventoryContent = Bukkit.createInventory(null, 9);
                        } else {
                            inventoryContent = SerializationUtils.inventoryFromBase64(content);
                        }

                        int backpackRows = rows != null ? rows : 1;
                        String backpackName = HeadBackpacks.getInstance().getCraftManager().getBackpackNameByID(type);
                        String title = backpackName != null ? backpackName : "Backpack";

                        event.getPlayer().openInventory(new PlacedBackpackGUI(title, backpackRows * 9, clickedBlock.getLocation(), inventoryContent).getInventory());
                        return;
                    }
                }
            }
        }

        if (!isPlayerHeadWithContent(event.getItem())) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType().isInteractable()) {
                return;
            }

            boolean allowPlace = plugin.getConfig().getBoolean("AllowPlaceBackpacks", true);
            if (allowPlace && event.getPlayer().isSneaking()) {
                return;
            }
        }

        event.setCancelled(true);

        String serializedContent = PersistentDataUtils.getStringData(event.getItem(), "content");
        Inventory inventoryContent;

        if (serializedContent.isEmpty())
            inventoryContent = Bukkit.createInventory(null, 9);
        else
            inventoryContent = SerializationUtils.inventoryFromBase64(serializedContent);

        int backpackRows = PersistentDataUtils.getIntData(event.getItem(), "rows");

        ItemMeta meta = event.getItem().getItemMeta();
        String displayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "Backpack";

        event.getPlayer().openInventory(new BackpackGUI(displayName, backpackRows * 9, event.getItem(), inventoryContent).getInventory());
    }
}
