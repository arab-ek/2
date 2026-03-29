package dev.arab.EVENTOWKICORE.eventowki;

import java.util.ArrayList;
import java.util.List;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class Excalibur extends EventItem {
  private final NamespacedKey killsKey;
  
  private List<String> configLore;
  
  public Excalibur(Main plugin) {
    super(plugin, "excalibur");
    this.killsKey = new NamespacedKey((Plugin)plugin, "excalibur_kills");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.configLore = this.plugin.getConfig().getStringList("meta.excalibur.lore");
  }
  
  public void onEntityKill(EntityDeathEvent event, Player killer, Player victim, ItemStack item) {
    if (item == null || !item.hasItemMeta())
      return; 
    ItemMeta meta = item.getItemMeta();
    int kills = ((Integer)meta.getPersistentDataContainer().getOrDefault(this.killsKey, PersistentDataType.INTEGER, Integer.valueOf(0))).intValue();
    int newKills = kills + 1;
    meta.getPersistentDataContainer().set(this.killsKey, PersistentDataType.INTEGER, Integer.valueOf(newKills));
    updateExcaliburLore(item, newKills, kills);
    applyHideFlags(meta);
    item.setItemMeta(meta);
    victim.getWorld().strikeLightningEffect(victim.getLocation());
  }
  
  public void onGive(Player player, ItemStack item) {
    if (item == null || !item.hasItemMeta())
      return; 
    ItemMeta meta = item.getItemMeta();
    meta.getPersistentDataContainer().set(this.killsKey, PersistentDataType.INTEGER, Integer.valueOf(0));
    meta.addEnchant(Enchantment.DAMAGE_ALL, 7, true);
    meta.addEnchant(Enchantment.DURABILITY, 3, true);
    applyHideFlags(meta);
    item.setItemMeta(meta);
    updateExcaliburLore(item, 0, 0);
  }
  
  public void updateExcaliburLore(ItemStack item, int newKills, int oldKills) {
    if (item == null || !item.hasItemMeta())
      return; 
    ItemMeta meta = item.getItemMeta();
    List<String> currentLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
    List<String> newTemplate = renderTemplate(newKills);
    if (currentLore.isEmpty()) {
      meta.setLore(newTemplate);
      applyHideFlags(meta);
      item.setItemMeta(meta);
      return;
    } 
    List<String> updatedLore = new ArrayList<>(currentLore);
    boolean[] templateFound = new boolean[this.configLore.size()];
    for (int i = 0; i < updatedLore.size(); i++) {
      String rawLine = ChatUtils.stripColor(updatedLore.get(i)).toLowerCase();
      int matchedIdx = -1;
      for (int j = 0; j < this.configLore.size(); j++) {
        String tLine = this.configLore.get(j);
        if (tLine.contains("%kills%") || tLine.contains("%bar%") || tLine.contains("%percentage%")) {
          String rawConfig = ChatUtils.stripColor(ChatUtils.color(tLine)).toLowerCase().replace("%kills%", " ").replace("%bar%", " ").replace("%percentage%", " ");
          String[] parts = rawConfig.split("[^a-z0-9ąęóśłżźń]+");
          boolean allPartsMatch = true;
          int matchesCount = 0;
          for (String part : parts) {
            if (part.trim().length() >= 2) {
              matchesCount++;
              if (!rawLine.contains(part.trim())) {
                allPartsMatch = false;
                break;
              } 
            } 
          } 
          boolean isBarLine = (tLine.contains("%bar%") && (rawLine.contains("---") || rawLine.contains("-") || (rawLine.contains("»") && rawLine.contains("%"))));
          boolean isKillsLine = (tLine.contains("%kills%") && (rawLine.contains("zabójstw") || rawLine.contains("kills") || rawLine.contains("zabojstw")));
          if ((matchesCount > 0 && allPartsMatch) || isBarLine || isKillsLine) {
            matchedIdx = j;
            break;
          } 
        } 
      } 
      if (matchedIdx != -1)
        if (!templateFound[matchedIdx]) {
          updatedLore.set(i, newTemplate.get(matchedIdx));
          templateFound[matchedIdx] = true;
        } else {
          updatedLore.remove(i);
          i--;
        }  
    } 
    for (int t = 0; t < this.configLore.size(); t++) {
      if (!templateFound[t]) {
        String tLine = this.configLore.get(t);
        if (tLine.contains("%kills%") || tLine.contains("%bar%") || tLine.contains("%percentage%"))
          updatedLore.add(newTemplate.get(t)); 
      } 
    } 
    meta.setLore(updatedLore);
    applyHideFlags(meta);
    item.setItemMeta(meta);
  }
  
  public boolean updateItem(ItemStack item, ItemMeta meta) {
    boolean changed = false;
    if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) < 7) {
      meta.addEnchant(Enchantment.DAMAGE_ALL, 7, true);
      changed = true;
    } 
    if (meta.getEnchantLevel(Enchantment.DURABILITY) < 3) {
      meta.addEnchant(Enchantment.DURABILITY, 3, true);
      changed = true;
    } 
    if (!meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
      applyHideFlags(meta);
      changed = true;
    } 
    return changed;
  }
  
  private List<String> renderTemplate(int kills) {
    List<String> rendered = new ArrayList<>();
    int pct = Math.min(100, kills);
    int filled = pct / 5;
    StringBuilder bar = new StringBuilder();
    for (int i = 0; i < 10; i++)
      bar.append((i < filled) ? "&a-" : "&8-"); 
    String barStr = bar.toString();
    for (String line : this.configLore)
      rendered.add(ChatUtils.color(line.replace("%kills%", String.valueOf(kills)).replace("%bar%", barStr).replace("%percentage%", String.valueOf(pct)))); 
    return rendered;
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\Excalibur.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */