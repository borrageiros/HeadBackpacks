package com.borrageiros.headbackpacks;

import com.borrageiros.headbackpacks.commands.BackpackCommand;
import com.borrageiros.headbackpacks.events.*;
import com.borrageiros.headbackpacks.manager.CraftManager;
import com.borrageiros.headbackpacks.manager.MessagesManager;
import com.borrageiros.headbackpacks.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeadBackpacks extends JavaPlugin {
    private static HeadBackpacks instance;
    private CraftManager craftManager;
    private MessagesManager messagesManager;
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

    public String getPluginPermission() {
        return pluginPermission;
    }

    public String getGivePermission() {
        return givePermission;
    }

    public String getTexturePermission() {
        return texturePermission;
    }

    @Override
    public void onEnable() {
        instance = this;
        registers();
        Bukkit.getConsoleSender().sendMessage(StringUtils.format("<green>[HeadBackpacks] has been started successfully!"));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getConsoleSender().sendMessage(StringUtils.format("<red>[HeadBackpacks] was successfully deactivated!"));
    }

    private void registers() {
        saveDefaultConfig();
        commands();
        events();
        this.messagesManager = new MessagesManager(this);
        this.craftManager = new CraftManager(this);
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
