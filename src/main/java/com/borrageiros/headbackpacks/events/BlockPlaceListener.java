package com.borrageiros.headbackpacks.events;

import com.borrageiros.headbackpacks.HeadBackpacks;
import com.borrageiros.headbackpacks.utils.items.ItemUtils;
import com.borrageiros.headbackpacks.utils.items.PersistentDataUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class BlockPlaceListener implements Listener {
    final Plugin plugin;

    public BlockPlaceListener(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.PLAYER_HEAD) return;
        if (!PersistentDataUtils.hasData(item, "type")) return;

        boolean allowPlace = plugin.getConfig().getBoolean("AllowPlaceBackpacks", true);
        if (!allowPlace || !event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlockPlaced();
        if (!(block.getState() instanceof Skull skull)) return;

        String type = PersistentDataUtils.getStringData(item, "type");
        String content = PersistentDataUtils.getStringData(item, "content");
        Integer rows = PersistentDataUtils.getIntData(item, "rows");
        String customTexture = PersistentDataUtils.getStringData(item, "custom_texture");

        PersistentDataContainer container = skull.getPersistentDataContainer();
        container.set(new NamespacedKey(HeadBackpacks.getInstance(), "type"), PersistentDataType.STRING, type);
        container.set(new NamespacedKey(HeadBackpacks.getInstance(), "content"), PersistentDataType.STRING, content != null ? content : "");
        container.set(new NamespacedKey(HeadBackpacks.getInstance(), "rows"), PersistentDataType.INTEGER, rows != null ? rows : 1);
        if (customTexture != null && !customTexture.isEmpty()) {
            container.set(new NamespacedKey(HeadBackpacks.getInstance(), "custom_texture"), PersistentDataType.STRING, customTexture);
        }

        skull.update();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return;
        if (!(block.getState() instanceof Skull skull)) return;

        PersistentDataContainer container = skull.getPersistentDataContainer();
        NamespacedKey typeKey = new NamespacedKey(HeadBackpacks.getInstance(), "type");

        if (!container.has(typeKey, PersistentDataType.STRING)) return;

        String type = container.get(typeKey, PersistentDataType.STRING);
        String content = container.get(new NamespacedKey(HeadBackpacks.getInstance(), "content"), PersistentDataType.STRING);
        Integer rows = container.get(new NamespacedKey(HeadBackpacks.getInstance(), "rows"), PersistentDataType.INTEGER);
        String customTexture = container.get(new NamespacedKey(HeadBackpacks.getInstance(), "custom_texture"), PersistentDataType.STRING);

        ItemStack backpackItem = HeadBackpacks.getInstance().getCraftManager().getResultByBackpackID(type);
        if (backpackItem == null) return;

        PersistentDataUtils.addStringData(backpackItem, "content", content != null ? content : "");
        PersistentDataUtils.addIntData(backpackItem, "rows", rows != null ? rows : 1);

        if (customTexture != null && !customTexture.isEmpty()) {
            ItemUtils.applyTexture(backpackItem, customTexture);
            PersistentDataUtils.addStringData(backpackItem, "custom_texture", customTexture);
        }

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), backpackItem);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem().getItemStack();
        if (item.getType() != Material.PLAYER_HEAD) return;
        if (!PersistentDataUtils.hasData(item, "type")) return;

        Bukkit.getScheduler().runTaskLater(plugin, player::updateInventory, 1L);
    }
}
