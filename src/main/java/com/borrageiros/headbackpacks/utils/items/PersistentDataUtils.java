package com.borrageiros.headbackpacks.utils.items;

import com.borrageiros.headbackpacks.HeadBackpacks;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PersistentDataUtils {

    public static void addStringData(ItemStack itemStack, String key, String data) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("ItemMeta is null. Cannot add persistent data to this item.");
        }
        if (data == null) data = "";
        meta.getPersistentDataContainer().set(buildKey(key), PersistentDataType.STRING, data);
        itemStack.setItemMeta(meta);
    }

    public static void addIntData(ItemStack itemStack, String key, int data) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("ItemMeta is null. Cannot add persistent data to this item.");
        }
        meta.getPersistentDataContainer().set(buildKey(key), PersistentDataType.INTEGER, data);
        itemStack.setItemMeta(meta);
    }

    public static boolean hasData(ItemStack itemStack, String key) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(buildKey(key));
    }

    public static String getStringData(ItemStack itemStack, String key) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return "";
        }
        String value = meta.getPersistentDataContainer().get(buildKey(key), PersistentDataType.STRING);
        return value == null ? "" : value;
    }

    public static int getIntData(ItemStack itemStack, String key) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return 1;
        }
        Integer value = meta.getPersistentDataContainer().get(buildKey(key), PersistentDataType.INTEGER);
        return value == null ? 1 : value;
    }

    public static NamespacedKey buildKey(String key) {
        return new NamespacedKey(HeadBackpacks.getInstance(), key);
    }
}

