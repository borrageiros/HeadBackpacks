package com.borrageiros.headbackpacks.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public static String format(String input) {
        if (input == null) return "";
        
        String result = input;
        
        result = result.replace("<black>", "&0");
        result = result.replace("<dark_blue>", "&1");
        result = result.replace("<dark_green>", "&2");
        result = result.replace("<dark_aqua>", "&3");
        result = result.replace("<dark_red>", "&4");
        result = result.replace("<dark_purple>", "&5");
        result = result.replace("<gold>", "&6");
        result = result.replace("<gray>", "&7");
        result = result.replace("<grey>", "&7");
        result = result.replace("<dark_gray>", "&8");
        result = result.replace("<dark_grey>", "&8");
        result = result.replace("<blue>", "&9");
        result = result.replace("<green>", "&a");
        result = result.replace("<aqua>", "&b");
        result = result.replace("<red>", "&c");
        result = result.replace("<light_purple>", "&d");
        result = result.replace("<yellow>", "&e");
        result = result.replace("<white>", "&f");
        
        result = result.replace("<obfuscated>", "&k");
        result = result.replace("<obf>", "&k");
        result = result.replace("<bold>", "&l");
        result = result.replace("<b>", "&l");
        result = result.replace("<strikethrough>", "&m");
        result = result.replace("<st>", "&m");
        result = result.replace("<underlined>", "&n");
        result = result.replace("<u>", "&n");
        result = result.replace("<italic>", "&o");
        result = result.replace("<i>", "&o");
        result = result.replace("<reset>", "&r");
        result = result.replace("<r>", "&r");
        
        Matcher matcher = HEX_PATTERN.matcher(result);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder hexColor = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                hexColor.append("&").append(c);
            }
            matcher.appendReplacement(buffer, hexColor.toString());
        }
        matcher.appendTail(buffer);
        result = buffer.toString();
        
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public static String formatItemName(String nameInput) {
        return format(nameInput);
    }
    
    public static String stripColor(String input) {
        return ChatColor.stripColor(format(input));
    }
}
