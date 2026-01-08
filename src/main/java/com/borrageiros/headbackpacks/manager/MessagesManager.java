package com.borrageiros.headbackpacks.manager;

import com.borrageiros.headbackpacks.HeadBackpacks;
import com.borrageiros.headbackpacks.entities.BackpackEntity;
import com.borrageiros.headbackpacks.placeholder.Placeholder;
import com.borrageiros.headbackpacks.placeholder.implementations.BackpackPlaceholder;
import com.borrageiros.headbackpacks.placeholder.implementations.MessagePlaceholder;
import com.borrageiros.headbackpacks.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MessagesManager {
    final Plugin plugin;
    private final Map<String, String> messages;
    private final Map<String, Function<Placeholder<?>, String>> placeholders;
    private String currentLanguage;

    public MessagesManager(Plugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.placeholders = new HashMap<>();
        loadMessages();
        registerPlaceholders();
    }

    private void loadMessages() {
        currentLanguage = this.plugin.getConfig().getString("Language", "en");
        Bukkit.getConsoleSender().sendMessage(StringUtils.format("<green>[HeadBackpacks] Loading language: " + currentLanguage));

        saveLanguageFiles();

        File langFile = new File(plugin.getDataFolder(), "lang/" + currentLanguage + ".yml");
        FileConfiguration langConfig;

        if (langFile.exists()) {
            langConfig = YamlConfiguration.loadConfiguration(langFile);
        } else {
            Bukkit.getConsoleSender().sendMessage(StringUtils.format("<yellow>[HeadBackpacks] Language file not found: " + currentLanguage + ".yml, using English as fallback"));
            langFile = new File(plugin.getDataFolder(), "lang/en.yml");
            if (!langFile.exists()) {
                this.plugin.getLogger().severe("No language files found!");
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                return;
            }
            langConfig = YamlConfiguration.loadConfiguration(langFile);
        }

        for (String key : langConfig.getKeys(false)) {
            String message = langConfig.getString(key);
            if (message == null || message.isEmpty()) {
                this.plugin.getLogger().warning(String.format("Message %s is null!", key));
                continue;
            }
            this.messages.put(key, message);
        }

        Bukkit.getConsoleSender().sendMessage(StringUtils.format("<green>[HeadBackpacks] Loaded " + messages.size() + " messages"));
    }

    private void saveLanguageFiles() {
        String[] languages = {"en", "es", "pt", "de", "fr", "zh", "ru"};
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        for (String lang : languages) {
            File langFile = new File(langFolder, lang + ".yml");
            if (!langFile.exists()) {
                plugin.saveResource("lang/" + lang + ".yml", false);
            }
        }
    }

    private void registerPlaceholders() {
        this.placeholders.put("%permission%", (param) -> HeadBackpacks.getInstance().getPluginPermission());
        this.placeholders.put("%backpack%", (param) -> {
            if (param instanceof MessagePlaceholder message) {
                return message.getPlaceholder();
            }
            if (param instanceof BackpackPlaceholder backpack) {
                return backpack.getPlaceholder().key();
            }
            return "";
        });
        this.placeholders.put("%backpack_name%", (param) -> {
            if (param instanceof MessagePlaceholder message) {
                return message.getPlaceholder();
            }
            if (param instanceof BackpackPlaceholder backpack) {
                return backpack.getPlaceholder().name();
            }
            return "";
        });
        this.placeholders.put("%backpack_rows%", (param) -> {
            if (param instanceof BackpackPlaceholder backpack) {
                return String.valueOf(backpack.getPlaceholder().rows());
            }
            return "";
        });
        this.placeholders.put("%backpack_slots%", (param) -> {
            if (param instanceof BackpackPlaceholder backpack) {
                return String.valueOf(backpack.getPlaceholder().rows() * 9);
            }
            return "";
        });
    }

    public String createMessage(String messageKey, Placeholder<?> param) {
        if (!this.messages.containsKey(messageKey))
            return StringUtils.format("<red>Message Not Found!");

        String message = this.messages.get(messageKey);
        for (String key : this.placeholders.keySet()) {
            if (!message.contains(key)) continue;
            String placeholder = this.placeholders.get(key).apply(param);
            message = message.replace(key, placeholder);
        }
        return StringUtils.format(message);
    }

    public List<String> createLore(BackpackEntity backpack) {
        List<String> formattedLore = new ArrayList<>();
        if (backpack.lore().isEmpty())
            return formattedLore;

        for (String line : backpack.lore()) {
            String formattedLine = line;
            for (String key : this.placeholders.keySet()) {
                if (!line.contains(key)) continue;
                String placeholder = this.placeholders.get(key).apply(new BackpackPlaceholder(backpack));
                formattedLine = formattedLine.replace(key, placeholder);
            }
            formattedLore.add(StringUtils.format(formattedLine));
        }

        return formattedLore;
    }
}
