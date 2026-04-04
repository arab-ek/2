package dev.arab.ADDONS.INCOGNITO;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
  private final JavaPlugin plugin;
  
  private FileConfiguration config;
  
  public ConfigManager(JavaPlugin plugin) {
    this.plugin = plugin;
    File configFile = new File(plugin.getDataFolder(), "addons/config-incognito.yml");
    if (!configFile.exists())
      plugin.saveResource("addons/config-incognito.yml", false);
    this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(configFile);
  }
  
  public String getIncognitoNick() {
    return this.config.getString("incognito.nick", "Gracz");
  }
  
  public String getSkinValue() {
    return this.config.getString("incognito.skin.value");
  }
  
  public String getSkinSignature() {
    return this.config.getString("incognito.skin.signature");
  }
  
  public void reloadConfig() {
    File configFile = new File(this.plugin.getDataFolder(), "addons/config-incognito.yml");
    this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(configFile);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\INCOGNITO\ConfigManager.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */