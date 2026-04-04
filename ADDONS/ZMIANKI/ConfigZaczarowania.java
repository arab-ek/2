package dev.arab.ADDONS.ZMIANKI;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ConfigZaczarowania {
  private final Main plugin;
  
  private FileConfiguration config;
  
  private File file;

  public ConfigZaczarowania(Main plugin) {
    this.plugin = plugin;
    File addonsFolder = new File(plugin.getDataFolder(), "addons");
    if (!addonsFolder.exists()) {
      addonsFolder.mkdirs();
    }

    this.file = new File(addonsFolder, "config-zaczarowania.yml");

    if (!this.file.exists()) {
      plugin.saveResource("addons/config-zaczarowania.yml", false);
    }

    this.config = (FileConfiguration) YamlConfiguration.loadConfiguration(this.file);
  }
  
  public void reload() {
    this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(this.file);
  }
  
  public FileConfiguration getConfig() {
    return this.config;
  }
  
  public String getString(String path) {
    return ChatUtils.color(this.config.getString(path, ""));
  }
  
  public List<String> getStringList(String path) {
    return (List<String>)this.config.getStringList(path).stream().map(ChatUtils::color).collect(Collectors.toList());
  }
  
  public ItemStack getItemFromConfig(String path) {
    Material material = Material.valueOf(this.config.getString(path + ".material", "STONE"));
    String name = getString(path + ".name");
    List<String> lore = getStringList(path + ".lore");
    int cmd = this.config.getInt(path + ".custom_model_data", 0);
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(name);
      meta.setLore(lore);
      if (cmd != 0)
        meta.setCustomModelData(Integer.valueOf(cmd)); 
      if (path.equals("enchant_item"))
        meta.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "is_zmianka"), PersistentDataType.BYTE, Byte.valueOf((byte)1)); 
      item.setItemMeta(meta);
    } 
    return item;
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\ZMIANKI\ConfigZaczarowania.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */