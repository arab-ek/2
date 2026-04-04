package dev.arab.ADDONS.GUI;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ModuleInventory {
  private final Main plugin;
  
  private FileConfiguration config;
  
  public ModuleInventory(Main plugin) {
    this.plugin = plugin;
    loadConfig();
  }
  
  public void loadConfig() {
    File configFile = new File(this.plugin.getDataFolder(), "addons/config-gui.yml");
    if (!configFile.exists())
      this.plugin.saveResource("addons/config-gui.yml", false);
    this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(configFile);
  }
  
  public void openInventory(Player player) {
    String title = ChatColor.translateAlternateColorCodes('&', this.config.getString("gui.title", "&8Wybierz tryb"));
    int size = this.config.getInt("gui.size", 27);
    Inventory inv = Bukkit.createInventory(new ModuleGuiHolder(), size, title);
    setupItems(inv, player);
    player.openInventory(inv);
  }
  
  public void setupItems(Inventory inv, Player player) {
    inv.setItem(this.config.getInt("gui.items.incognito.slot"), createItem("incognito", player));
    inv.setItem(this.config.getInt("gui.items.trybtworcy.slot"), createItem("trybtworcy", player));
  }
  
  private ItemStack createItem(String key, Player player) {
    String path = "gui.items." + key + ".";
    Material material = Material.getMaterial(this.config.getString(path + "material", "STONE"));
    if (material == null)
      material = Material.STONE; 
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.config.getString(path + "name", "")));
      List<String> lore = new ArrayList<>();
      boolean active = false;
      if (key.equals("incognito")) {
        active = this.plugin.getIncognitoManager().isIncognito(player.getUniqueId());
      } else {
        active = this.plugin.getTrybTworcyManager().hasModeEnabled(player);
      } 
      String status = active ? "&aWłączony" : "&cWyłączony";
      for (String line : this.config.getStringList(path + "lore"))
        lore.add(ChatColor.translateAlternateColorCodes('&', line.replace("{STATUS}", status))); 
      meta.setLore(lore);
      meta.setCustomModelData(Integer.valueOf(this.config.getInt(path + "custommodeldata", 0)));
      item.setItemMeta(meta);
      if (material == Material.PLAYER_HEAD && this.config.contains(path + "head_value")) {
        String value = this.config.getString(path + "head_value");
        if (value != null && !value.isEmpty())
          item = applyHeadValue(item, value); 
      } 
    } 
    return item;
  }
  
  private ItemStack applyHeadValue(ItemStack item, String value) {
    SkullMeta skullMeta;
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta instanceof SkullMeta) {
      skullMeta = (SkullMeta)itemMeta;
    } else {
      return item;
    } 
    try {
      UUID id = UUID.randomUUID();
      PlayerProfile profile = Bukkit.createProfile(id, "Skull");
      profile.setProperty(new ProfileProperty("textures", value));
      skullMeta.setPlayerProfile(profile);
      item.setItemMeta((ItemMeta)skullMeta);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return item;
  }
  
  public FileConfiguration getConfig() {
    return this.config;
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\GUI\ModuleInventory.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */