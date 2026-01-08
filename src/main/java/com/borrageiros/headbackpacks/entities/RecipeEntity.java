package com.borrageiros.headbackpacks.entities;

import org.bukkit.inventory.ItemStack;

public record RecipeEntity(String backpackID, ItemStack[] shape, ItemStack result) {
}

