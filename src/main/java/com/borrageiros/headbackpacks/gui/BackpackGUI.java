package com.borrageiros.headbackpacks.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class BackpackGUI implements InventoryHolder {
    private final Inventory inventory;
    private final ItemStack backpack;

    public BackpackGUI(String title, int slots, ItemStack itemStack, Inventory content) {
        this.backpack = itemStack;
        this.inventory = Bukkit.createInventory(this, slots, title);
        this.inventory.setContents(content.getContents());
    }

    public ItemStack getBackpack() {
        return backpack;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
