package com.borrageiros.headbackpacks.manager;

import com.borrageiros.headbackpacks.HeadBackpacks;
import com.borrageiros.headbackpacks.entities.BackpackEntity;
import com.borrageiros.headbackpacks.entities.RecipeEntity;
import com.borrageiros.headbackpacks.utils.StringUtils;
import com.borrageiros.headbackpacks.utils.items.ItemUtils;
import com.borrageiros.headbackpacks.utils.items.PersistentDataUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class CraftManager {
    final Plugin plugin;
    private final List<RecipeEntity> backpackRecipes;
    private final Map<String, BackpackEntity> registeredBackpacks;

    public Map<String, BackpackEntity> getRegisteredBackpacks() {
        return registeredBackpacks;
    }

    public CraftManager(Plugin plugin) {
        this.plugin = plugin;
        this.backpackRecipes = new ArrayList<>();
        this.registeredBackpacks = new HashMap<>();
        loadRecipes();
    }

    private void loadRecipes() {
        Bukkit.getConsoleSender().sendMessage(StringUtils.format("<green>[HeadBackpacks] Starting the process of loading the backpacks!"));
        ConfigurationSection backpackSection = this.plugin.getConfig().getConfigurationSection("Backpacks");
        if (backpackSection == null) {
            this.plugin.getLogger().severe("Backpack List not found in Config!");
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            return;
        }

        for (String backpack : backpackSection.getKeys(false)) {
            ConfigurationSection internalBackpackSection = backpackSection.getConfigurationSection(backpack);
            if (internalBackpackSection == null) {
                this.plugin.getLogger().severe(String.format("Backpack internal settings not found! (%s)", backpack));
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                return;
            }

            String backpackName = internalBackpackSection.getString("name");
            List<String> backpackLore = internalBackpackSection.getStringList("lore");
            int backpackRows = internalBackpackSection.getInt("rows");
            if (backpackRows == 0) backpackRows = 1;

            this.registeredBackpacks.put(backpack, new BackpackEntity(backpack, backpackName, backpackLore, backpackRows));

            String defaultTexture = this.plugin.getConfig().getString("DefaultTexture", "https://textures.minecraft.net/texture/bdfcee8306e0434a747710e2bdc2558c5a222e9b693477909f10d6cc220f6764");
            String textureUrl = internalBackpackSection.getString("texture", defaultTexture);
            ItemStack backpackItemStack = ItemUtils.createSkullItemStack(textureUrl, backpackName, HeadBackpacks.getInstance().getMessagesManager().createLore(this.registeredBackpacks.get(backpack)));
            PersistentDataUtils.addStringData(backpackItemStack, "type", backpack);
            PersistentDataUtils.addStringData(backpackItemStack, "content", "");
            PersistentDataUtils.addIntData(backpackItemStack, "rows", backpackRows);

            ConfigurationSection craftInternalBackpackSection = internalBackpackSection.getConfigurationSection("craft");
            if (craftInternalBackpackSection == null) {
                this.plugin.getLogger().severe(String.format("Backpack craft settings not found! (%s)", backpack));
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                return;
            }

            ItemStack[] backpackShape = new ItemStack[9];
            List<String> backpackCraftShape = craftInternalBackpackSection.getStringList("shape");

            ConfigurationSection itemsCraftInternalBackpackSection = craftInternalBackpackSection.getConfigurationSection("items");
            if (itemsCraftInternalBackpackSection == null) {
                this.plugin.getLogger().severe(String.format("Backpack material settings no found! (%s)", backpack));
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                return;
            }

            Map<String, ItemStack> backpackCraftMaterials = new HashMap<>();
            for (String key : itemsCraftInternalBackpackSection.getKeys(false)) {
                String stringMaterial = itemsCraftInternalBackpackSection.getString(key.toUpperCase());
                if (stringMaterial == null) {
                    this.plugin.getLogger().severe(String.format("Item key %s is null! (%s)", key, backpack));
                    this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                    return;
                }

                ItemStack item;

                if (key.equals("$")) {
                    ItemStack backpackRecoveredItem = getResultByBackpackID(stringMaterial);
                    if (backpackRecoveredItem == null) {
                        this.plugin.getLogger().severe(String.format("The referenced Backpack (%s) was not found! (%s)", stringMaterial, backpack));
                        this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                        return;
                    }

                    item = backpackRecoveredItem;
                } else {
                    Material material = Material.matchMaterial(stringMaterial);
                    if (material == null) {
                        this.plugin.getLogger().severe(String.format("The material was not found! (%s)", stringMaterial));
                        this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                        return;
                    }

                    item = new ItemStack(material);
                }

                backpackCraftMaterials.put(key, new ItemStack(item));
            }

            int shapeIndex = 0;
            for (String shapeLine : backpackCraftShape) {
                for (int i = 0; i < shapeLine.length(); i++) {
                    String materialKey = String.valueOf(shapeLine.charAt(i));
                    if (materialKey.equals(" "))
                        backpackShape[shapeIndex] = null;
                    else
                        backpackShape[shapeIndex] = new ItemStack(backpackCraftMaterials.get(materialKey));

                    shapeIndex++;
                }
            }
            this.backpackRecipes.add(new RecipeEntity(backpack, backpackShape, backpackItemStack));
            Bukkit.getConsoleSender().sendMessage(StringUtils.format(String.format("<green>[HeadBackpacks] %s has been loaded Successfully!", backpack)));
        }
    }

    public ItemStack getResultByBackpackID(String backpackID) {
        for (RecipeEntity recipe : this.backpackRecipes) {
            if (recipe.backpackID().equals(backpackID))
                return recipe.result().clone();
        }
        return null;
    }

    public String getBackpackNameByID(String backpackID) {
        BackpackEntity backpack = this.registeredBackpacks.get(backpackID);
        if (backpack != null) {
            return backpack.name();
        }
        return null;
    }

    public RecipeEntity isCustomCraft(ItemStack[] craftMatrix) {
        ItemStack[] matrixCopy = Arrays.copyOf(craftMatrix, craftMatrix.length);
        for (int i = 0; i < matrixCopy.length; i++) {
            if (matrixCopy[i] != null) {
                if (matrixCopy[i].getType().equals(Material.PLAYER_HEAD)) {
                    String backpackID = PersistentDataUtils.getStringData(matrixCopy[i], "type");
                    matrixCopy[i] = getResultByBackpackID(backpackID);
                }
            }
        }

        for (RecipeEntity recipe : this.backpackRecipes) {
            if (Arrays.equals(recipe.shape(), matrixCopy)) {
                return recipe;
            }
        }
        return null;
    }
}
