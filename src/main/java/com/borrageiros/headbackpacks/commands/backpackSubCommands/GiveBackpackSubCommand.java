package com.borrageiros.headbackpacks.commands.backpackSubCommands;

import com.borrageiros.headbackpacks.HeadBackpacks;
import com.borrageiros.headbackpacks.entities.BackpackEntity;
import com.borrageiros.headbackpacks.placeholder.implementations.MessagePlaceholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveBackpackSubCommand {

    public void onCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("give_command_usage", new MessagePlaceholder("")));
            return;
        }

        ItemStack backpackItemStack = HeadBackpacks.getInstance().getCraftManager().getResultByBackpackID(args[1]);

        if (backpackItemStack == null) {
            player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("backpack_not_found", new MessagePlaceholder(args[1])));
            return;
        }

        player.getInventory().addItem(backpackItemStack);
        BackpackEntity backpack = HeadBackpacks.getInstance().getCraftManager().getRegisteredBackpacks().get(args[1]);
        player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("give_command_success", new MessagePlaceholder(backpack.name())));
    }
}

