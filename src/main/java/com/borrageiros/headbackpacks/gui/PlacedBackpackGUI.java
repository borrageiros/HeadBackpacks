package com.borrageiros.headbackpacks.gui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PlacedBackpackGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Location blockLocation;

    public PlacedBackpackGUI(String title, int slots, Location blockLocation, Inventory content) {
        this.blockLocation = blockLocation;
        this.inventory = Bukkit.createInventory(this, slots, title);
        this.inventory.setContents(content.getContents());
    }

    public Location getBlockLocation() {
        return blockLocation;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
