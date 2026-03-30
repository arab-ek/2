package dev.arab.TOOLS.CLEANER;

import dev.arab.Main;
import dev.arab.TOOLS.utils.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigCleaner {
    private final Main plugin;
    private File file;
    private FileConfiguration config;

    public ConfigCleaner(Main plugin) {
        this.plugin = plugin;
        File addonsFolder = new File(plugin.getDataFolder(), "addons");
        if (!addonsFolder.exists()) {
            addonsFolder.mkdirs();
        }

        this.file = new File(addonsFolder, "config-cleaner.yml");
        if (!this.file.exists()) {
            plugin.saveResource("tools/config-cleaner.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getMessage(String path) {
        return ColorUtil.fixColor(config.getString("messages." + path, ""));
    }
}