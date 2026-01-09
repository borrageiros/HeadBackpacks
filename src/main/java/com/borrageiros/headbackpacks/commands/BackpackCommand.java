package com.borrageiros.headbackpacks.commands;

import com.borrageiros.headbackpacks.HeadBackpacks;
import com.borrageiros.headbackpacks.commands.backpackSubCommands.GiveBackpackSubCommand;
import com.borrageiros.headbackpacks.commands.backpackSubCommands.ListBackpackSubCommand;
import com.borrageiros.headbackpacks.commands.backpackSubCommands.TextureBackpackSubCommand;
import com.borrageiros.headbackpacks.placeholder.implementations.MessagePlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BackpackCommand implements CommandExecutor {
    final Plugin plugin;

    public BackpackCommand(Plugin plugin) {
        this.plugin = plugin;
        PluginCommand command = this.plugin.getServer().getPluginCommand("backpack");
        if (command == null)
            throw new RuntimeException("Error in Backpack Command!");
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            HeadBackpacks.getInstance().getLogger().warning("Only players can execute this Command!");
            return true;
        }

        if (!player.hasPermission(HeadBackpacks.getInstance().getPluginPermission())) {
            player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("dont_have_permission", new MessagePlaceholder("")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("backpack_command_usage", new MessagePlaceholder("")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (!player.hasPermission(HeadBackpacks.getInstance().getGivePermission())) {
                    player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("dont_have_permission", new MessagePlaceholder("")));
                    return true;
                }
                new GiveBackpackSubCommand().onCommand(player, args);
            }
            case "list" -> new ListBackpackSubCommand().onCommand(player, args);
            case "texture" -> {
                if (!player.hasPermission(HeadBackpacks.getInstance().getTexturePermission())) {
                    player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("dont_have_permission", new MessagePlaceholder("")));
                    return true;
                }
                new TextureBackpackSubCommand().onCommand(player, args);
            }
            default -> player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("backpack_command_usage", new MessagePlaceholder("")));
        }
        return true;
    }
}
