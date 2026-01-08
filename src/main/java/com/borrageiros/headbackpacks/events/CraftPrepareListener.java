package com.borrageiros.headbackpacks.events;

import com.borrageiros.headbackpacks.HeadBackpacks;
import com.borrageiros.headbackpacks.entities.RecipeEntity;
import com.borrageiros.headbackpacks.utils.items.PersistentDataUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class CraftPrepareListener implements Listener {
    final Plugin plugin;

    public CraftPrepareListener(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCraftInteract(PrepareItemCraftEvent event) {
        if (!event.getInventory().getType().equals(InventoryType.WORKBENCH)) return;
        RecipeEntity recipe = HeadBackpacks.getInstance().getCraftManager().isCustomCraft(event.getInventory().getMatrix());
        if (recipe == null) return;

        ItemStack backpack = null;
        for (int i = 0; i < event.getInventory().getMatrix().length; i++) {
            ItemStack currentItem = event.getInventory().getMatrix()[i];
            if (currentItem == null) continue;
            if (currentItem.getType().equals(Material.PLAYER_HEAD) && PersistentDataUtils.hasData(currentItem, "type"))
                backpack = event.getInventory().getMatrix()[i];
        }

        String oldContent = "";
        if (backpack != null)
            oldContent = PersistentDataUtils.getStringData(backpack, "content");

        ItemStack result = recipe.result().clone();
        PersistentDataUtils.addStringData(result, "content", oldContent);
        event.getInventory().setResult(result);
    }
}
