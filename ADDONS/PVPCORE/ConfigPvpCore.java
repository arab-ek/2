package dev.arab.ADDONS.PVPCORE;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigPvpCore {
  private final JavaPlugin plugin;
  
  private FileConfiguration config;
  
  private final File configFile;
  
  public ConfigPvpCore(JavaPlugin plugin) {
    this.plugin = plugin;
    this.configFile = new File(plugin.getDataFolder(), "addons/config-pvpcore.yml");
    if (!this.configFile.exists())
      plugin.saveResource("addons/config-pvpcore.yml", false);
    reloadConfig();
  }
  
  public void reloadConfig() {
    this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(this.configFile);
  }
  
  public double getDamageLimit() {
    return this.config.getDouble("damage-limit", 6.0D) * 2.0D;
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\PVPCORE\ConfigPvpCore.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */