package dev.arab.TOOLS.EXPLOSIONS;

import dev.arab.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigExplosions {
    private final Main plugin;
    private File file;
    private FileConfiguration config;

    public ConfigExplosions(Main plugin) {
        this.plugin = plugin;
        File addonsFolder = new File(plugin.getDataFolder(), "addons");
        if (!addonsFolder.exists()) addonsFolder.mkdirs();

        this.file = new File(addonsFolder, "config-explosions.yml");
        if (!this.file.exists()) {
            plugin.saveResource("tools/config-explosions.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}