package com.borrageiros.headbackpacks.events;

import com.borrageiros.headbackpacks.utils.items.PersistentDataUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class AnvilPrepareListener implements Listener {
    final Plugin plugin;

    public AnvilPrepareListener(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAnvilInteract(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getItem(0);
        if (firstItem == null) return;
        if (!firstItem.getType().equals(Material.PLAYER_HEAD)) return;
        if (!PersistentDataUtils.hasData(firstItem, "type")) return;

        ItemStack secondItem = event.getInventory().getItem(1);
        if (secondItem != null && secondItem.getType() != Material.AIR) {
            event.setResult(null);
            return;
        }

        String renameText = event.getInventory().getRenameText();
        if (renameText == null || renameText.isEmpty()) {
            return;
        }

        ItemStack result = firstItem.clone();
        ItemMeta resultMeta = result.getItemMeta();
        if (resultMeta == null) {
        event.setResult(null);
            return;
        }

        resultMeta.setDisplayName(renameText);
        result.setItemMeta(resultMeta);

        event.setResult(result);
    }
}
