package com.borrageiros.headbackpacks;

import com.borrageiros.headbackpacks.commands.BackpackCommand;
import com.borrageiros.headbackpacks.events.*;
import com.borrageiros.headbackpacks.manager.CraftManager;
import com.borrageiros.headbackpacks.manager.MessagesManager;
import com.borrageiros.headbackpacks.manager.BackpackVisualManager;
import com.borrageiros.headbackpacks.utils.StringUtils;
import com.borrageiros.headbackpacks.utils.items.PersistentDataUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeadBackpacks extends JavaPlugin {
    private static HeadBackpacks instance;
    private CraftManager craftManager;
    private MessagesManager messagesManager;
    private BackpackVisualManager visualManager;
    private String pluginPermission;
    private String givePermission;
    private String texturePermission;

    public static HeadBackpacks getInstance() {
        return instance;
    }

    public CraftManager getCraftManager() {
        return craftManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public BackpackVisualManager getVisualManager() {
        return visualManager;
    }

    public String getPluginPermission() {
        return pluginPermission;
    }

    public String getGivePermission() {
        return givePermission;
    }

    public String getTexturePermission() {
        return texturePermission;
    }

    public boolean isVisualBackpacksEnabled() {
        return this.getConfig().getBoolean("EnableVisualBackpacks", false);
    }

    @Override
    public void onEnable() {
        instance = this;
        registers();
        checkOnlinePlayers();
        Bukkit.getConsoleSender().sendMessage(StringUtils.format("<green>[HeadBackpacks] has been started successfully!"));
    }

    private void checkOnlinePlayers() {
        if (!isVisualBackpacksEnabled()) return;
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack chestplate = player.getInventory().getChestplate();
                if (chestplate != null && chestplate.getType() == Material.PLAYER_HEAD) {
                    if (PersistentDataUtils.hasData(chestplate, "type")) {
                        this.visualManager.spawnBackpackFor(player, chestplate);
                    }
                }
            }
        }, 10L);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        if (this.visualManager != null) this.visualManager.cleanup();
        Bukkit.getConsoleSender().sendMessage(StringUtils.format("<red>[HeadBackpacks] was successfully deactivated!"));
    }

    private void registers() {
        saveDefaultConfig();
        commands();
        events();
        this.messagesManager = new MessagesManager(this);
        this.craftManager = new CraftManager(this);
        this.visualManager = new BackpackVisualManager(this);
        String permission = this.getConfig().getString("Permission");
        if (permission == null) permission = "headbackpacks.use";
        this.pluginPermission = permission;

        String givePermission = this.getConfig().getString("GivePermission");
        if (givePermission == null) givePermission = "headbackpacks.give";
        this.givePermission = givePermission;

        String texturePermission = this.getConfig().getString("TexturePermission");
        if (texturePermission == null) texturePermission = "headbackpacks.texture";
        this.texturePermission = texturePermission;
    }

    private void commands() {
        new BackpackCommand(this);
    }

    private void events() {
        new PlayerInteractListener(this);
        new AnvilPrepareListener(this);
        new PlayerInventoryListener(this);
        new CraftPrepareListener(this);
        new BlockPlaceListener(this);
    }
}
