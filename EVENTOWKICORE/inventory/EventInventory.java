package dev.arab.EVENTOWKICORE.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import dev.arab.EVENTOWKICORE.eventowki.EventItem;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.EVENTOWKICORE.utils.SkullUtils;
import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class EventInventory {
  private final Main plugin;
  
  public EventInventory(Main plugin) {
    this.plugin = plugin;
  }
  
  public void open(Player player) {
    String title = this.plugin.getMessagesConfig().getString("gui.title", "Event Items");
    int size = this.plugin.getConfig().getInt("gui.size", 54);
    Inventory inv = Bukkit.createInventory(null, size, ChatUtils.color(title));
    ConfigurationSection metaSection = this.plugin.getConfig().getConfigurationSection("meta");
    if (metaSection != null) {
      addItem(inv, metaSection, "cieple_mleko", 0, Material.MILK_BUCKET, player);
      addItem(inv, metaSection, "rozga", 1, Material.STICK, player);
      addItem(inv, metaSection, "lewe_jajko", 2, Material.EGG, player);
      addItem(inv, metaSection, "bombarda_maxina", 3, Material.FIRE_CHARGE, player);
      addItem(inv, metaSection, "sniezka", 4, Material.SNOWBALL, player);
      addItem(inv, metaSection, "lopata_grincha", 5, Material.DIAMOND_SHOVEL, player);
      addItem(inv, metaSection, "kosa", 6, Material.NETHERITE_HOE, player);
      addItem(inv, metaSection, "marchewkowy_miecz", 7, Material.GOLDEN_SWORD, player);
      addItem(inv, metaSection, "marchewkowa_kusza", 8, Material.CROSSBOW, player);
      addItem(inv, metaSection, "wedka_surferka", 9, Material.FISHING_ROD, player);
      addItem(inv, metaSection, "piernik", 10, Material.COOKIE, player);
      addItem(inv, metaSection, "piekielna_tarcza", 11, Material.SHIELD, player);
      addItem(inv, metaSection, "dynamit", 12, Material.RED_CANDLE, player);
      addItem(inv, metaSection, "zatruty_olowek", 13, Material.LIME_CANDLE, player);
      addItem(inv, metaSection, "roza_kupidyna", 14, Material.POPPY, player);
      addItem(inv, metaSection, "krew_wampira", 15, Material.BEETROOT_SOUP, player);
      addItem(inv, metaSection, "splesniala_kanapka", 16, Material.POISONOUS_POTATO, player);
      addItem(inv, metaSection, "parawan", 17, Material.BLUE_BANNER, player);
      addItem(inv, metaSection, "turbo_trap", 18, Material.EGG, player);
      addItem(inv, metaSection, "siekiera_grincha", 19, Material.GOLDEN_AXE, player);
      addItem(inv, metaSection, "boski_topor", 20, Material.IRON_AXE, player);
      addItem(inv, metaSection, "kostka_rubika", 21, Material.PLAYER_HEAD, player);
      addItem(inv, metaSection, "balonik_z_helem", 22, Material.PLAYER_HEAD, player);
      addItem(inv, metaSection, "lizak", 23, Material.ALLIUM, player);
      addItem(inv, metaSection, "przeterminowany_trunek", 24, Material.SPLASH_POTION, player);
      addItem(inv, metaSection, "excalibur", 25, Material.NETHERITE_SWORD, player);
      addItem(inv, metaSection, "zlamane_serce", 26, Material.PURPLE_DYE, player);
      addItem(inv, metaSection, "korona_anarchii", 27, Material.GOLDEN_HELMET, player);
      addItem(inv, metaSection, "wzmocniona_elytra", 28, Material.ELYTRA, player);
      addItem(inv, metaSection, "wedka_nielota", 29, Material.FISHING_ROD, player);
      addItem(inv, metaSection, "kupa_anarchii", 30, Material.GRAY_DYE, player);
      addItem(inv, metaSection, "zajeczy_miecz", 31, Material.NETHERITE_SWORD, player);
      addItem(inv, metaSection, "jajko_creepera", 32, Material.CREEPER_SPAWN_EGG, player);
      addItem(inv, metaSection, "wata_cukrowa", 33, Material.PINK_DYE, player);
      addItem(inv, metaSection, "totem_ulaskawienia", 34, Material.TOTEM_OF_UNDYING, player);
      addItem(inv, metaSection, "cudowna_latarnia", 35, Material.BEACON, player);
      addItem(inv, metaSection, "rozdzka_iluzjonisty", 36, Material.GOLDEN_HOE, player);
      addItem(inv, metaSection, "trojzab_posejdona", 37, Material.BOW, player);
      addItem(inv, metaSection, "sakiewka_dropu", 38, Material.RABBIT_FOOT, player);
      addItem(inv, metaSection, "rog_jednorozca", 39, Material.END_ROD, player);
      addItem(inv, metaSection, "roza_kupidyna_2026", 40, Material.POPPY, player);
      addItem(inv, metaSection, "piekielny_miecz", 41, Material.NETHERITE_SWORD, player);
      addItem(inv, metaSection, "arcus_magnus", 42, Material.BOW, player);
      addItem(inv, metaSection, "wampirze_jablko", 43, Material.GOLDEN_APPLE, player);
      addItem(inv, metaSection, "rozgotowana_kukurydza", 44, Material.CARROT, player);
      addItem(inv, metaSection, "hydro_klatka", 45, Material.FIRE_CHARGE, player);
      addItem(inv, metaSection, "blok_widmo", 46, Material.STRUCTURE_BLOCK, player);
      addItem(inv, metaSection, "smoczy_miecz", 47, Material.DIAMOND_SWORD, player);
      addItem(inv, metaSection, "luk_kupidyna", 48, Material.BOW, player);
    } 
    player.openInventory(inv);
  }
  
  private void addItem(Inventory inv, ConfigurationSection section, String key, int slot, Material defaultMaterial, Player player) {
    ItemStack item;
    ConfigurationSection itemSection = section.getConfigurationSection(key);
    if (itemSection == null)
      return; 
    String materialName = itemSection.getString("material");
    Material mat = defaultMaterial;
    if (materialName != null && !materialName.isEmpty())
      try {
        mat = Material.valueOf(materialName.toUpperCase());
      } catch (IllegalArgumentException illegalArgumentException) {} 
    String textureValue = itemSection.getString("texture-value");
    if (mat == Material.PLAYER_HEAD && textureValue != null && !textureValue.isEmpty()) {
      item = SkullUtils.getCustomSkull(textureValue);
    } else {
      item = new ItemStack(mat);
    } 
    ItemMeta meta = item.getItemMeta();
    if (meta == null)
      return; 
    meta.setDisplayName(ChatUtils.color(itemSection.getString("name", key)));
    List<String> coloredLore = new ArrayList<>();
    for (String line : itemSection.getStringList("lore"))
      coloredLore.add(ChatUtils.color(line)); 
    meta.setLore(coloredLore);
    NamespacedKey itemKey = new NamespacedKey((Plugin)this.plugin, "event_item_id");
    meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, key);
    if (!key.equals("krew_wampira")) {
      NamespacedKey uidKey = new NamespacedKey((Plugin)this.plugin, "event_item_uid");
      meta.getPersistentDataContainer().set(uidKey, PersistentDataType.STRING, UUID.randomUUID().toString());
    } 
    int customModelData = itemSection.getInt("custom_model_data", 0);
    if (customModelData > 0)
      meta.setCustomModelData(Integer.valueOf(customModelData)); 
    meta.setUnbreakable(true);
    item.setItemMeta(meta);
    EventItem eventItem = this.plugin.getEventItemManager().getItemById(key);
    if (eventItem != null)
      eventItem.onGive(player, item); 
    inv.setItem(slot, item);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\inventory\EventInventory.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */