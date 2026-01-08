package com.borrageiros.headbackpacks.utils.items;

import com.borrageiros.headbackpacks.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ItemUtils {
    
    public static ItemStack createSkullItemStack(String textureUrl, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

        applyTextureToMeta(skullMeta, textureUrl);

        skullMeta.setDisplayName(StringUtils.formatItemName(name));
        skullMeta.setLore(lore);
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

    public static void applyTexture(ItemStack itemStack, String textureUrl) {
        if (itemStack.getType() != Material.PLAYER_HEAD) return;
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        if (skullMeta == null) return;

        applyTextureToMeta(skullMeta, textureUrl);

        itemStack.setItemMeta(skullMeta);
    }
    
    private static void applyTextureToMeta(SkullMeta skullMeta, String textureUrl) {
        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(textureUrl));
            profile.setTextures(textures);
            skullMeta.setOwnerProfile(profile);
        } catch (Exception e) {
            applyTextureWithReflection(skullMeta, textureUrl);
        }
    }
    
    private static void applyTextureWithReflection(SkullMeta skullMeta, String textureUrl) {
        try {
            String textureValue = Base64.getEncoder().encodeToString(
                    ("{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}").getBytes()
            );
            
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            
            Object gameProfile = gameProfileClass
                    .getConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), "");
            
            Object property = propertyClass
                    .getConstructor(String.class, String.class)
                    .newInstance("textures", textureValue);
            
            Method getPropertiesMethod = gameProfileClass.getMethod("getProperties");
            Object propertyMap = getPropertiesMethod.invoke(gameProfile);
            
            Method putMethod = propertyMap.getClass().getMethod("put", Object.class, Object.class);
            putMethod.invoke(propertyMap, "textures", property);
            
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, gameProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
