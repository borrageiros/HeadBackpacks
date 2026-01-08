package com.borrageiros.headbackpacks.commands.backpackSubCommands;

import com.borrageiros.headbackpacks.HeadBackpacks;
import com.borrageiros.headbackpacks.entities.BackpackEntity;
import com.borrageiros.headbackpacks.placeholder.implementations.BackpackPlaceholder;
import com.borrageiros.headbackpacks.placeholder.implementations.MessagePlaceholder;
import org.bukkit.entity.Player;

public class ListBackpackSubCommand {

    public void onCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("backpack_command_usage", new MessagePlaceholder("")));
            return;
        }

        String messageHeader = HeadBackpacks.getInstance().getMessagesManager().createMessage("list_command_message", new MessagePlaceholder(""));
        player.sendMessage(messageHeader);

        for (BackpackEntity backpack : HeadBackpacks.getInstance().getCraftManager().getRegisteredBackpacks().values()) {
            player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("list_command_items_message", new BackpackPlaceholder(backpack)));
        }
    }
}
