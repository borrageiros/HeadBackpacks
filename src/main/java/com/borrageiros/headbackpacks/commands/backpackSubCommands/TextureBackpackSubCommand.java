package com.borrageiros.headbackpacks.commands.backpackSubCommands;

import com.borrageiros.headbackpacks.HeadBackpacks;
import com.borrageiros.headbackpacks.placeholder.implementations.MessagePlaceholder;
import com.borrageiros.headbackpacks.utils.items.ItemUtils;
import com.borrageiros.headbackpacks.utils.items.PersistentDataUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TextureBackpackSubCommand {

    public void onCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("texture_command_usage", new MessagePlaceholder("")));
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() != Material.PLAYER_HEAD || !PersistentDataUtils.hasData(itemInHand, "type")) {
            player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("texture_command_no_backpack", new MessagePlaceholder("")));
            return;
        }

        if (itemInHand.getAmount() > 1) {
            player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("texture_command_stack_too_large", new MessagePlaceholder("")));
            return;
        }

        String textureUrl = args[1];
        if (!isValidTextureUrl(textureUrl)) {
            player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("texture_command_invalid_url", new MessagePlaceholder("")));
            return;
        }

        ItemUtils.applyTexture(itemInHand, textureUrl);
        PersistentDataUtils.addStringData(itemInHand, "custom_texture", textureUrl);

        player.sendMessage(HeadBackpacks.getInstance().getMessagesManager().createMessage("texture_command_success", new MessagePlaceholder("")));
    }

    private boolean isValidTextureUrl(String url) {
        return url.startsWith("https://textures.minecraft.net/texture/") ||
               url.startsWith("http://textures.minecraft.net/texture/");
    }
}

