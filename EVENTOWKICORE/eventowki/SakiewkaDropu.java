package dev.arab.EVENTOWKICORE.eventowki;

import java.util.List;
import java.util.UUID;
import dev.arab.EVENTOWKICORE.inventory.SakiewkaInventory;
import dev.arab.EVENTOWKICORE.utils.ItemSerializer;
import dev.arab.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class SakiewkaDropu extends EventItem {
  public SakiewkaDropu(Main plugin) {
    super(plugin, "sakiewka_dropu");
  }
  
  public void onGive(Player player, ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.getPersistentDataContainer().set(this.uidKey, PersistentDataType.STRING, UUID.randomUUID().toString());
      item.setItemMeta(meta);
    } 
  }
  
  public boolean updateItem(ItemStack item, ItemMeta meta) {
    if (meta == null)
      return false; 
    boolean changed = false;
    if (!meta.getPersistentDataContainer().has(this.uidKey, PersistentDataType.STRING)) {
      meta.getPersistentDataContainer().set(this.uidKey, PersistentDataType.STRING, UUID.randomUUID().toString());
      changed = true;
    } 
    String legacyKey = "sakiewka_contents";
    NamespacedKey storageKey = new NamespacedKey((Plugin)this.plugin, legacyKey);
    if (meta.getPersistentDataContainer().has(storageKey, PersistentDataType.STRING)) {
      String oldData = (String)meta.getPersistentDataContainer().get(storageKey, PersistentDataType.STRING);
      if (oldData != null) {
        List<ItemStack> stored = ItemSerializer.deserializeItems(oldData);
        meta.getPersistentDataContainer().set(storageKey, PersistentDataType.BYTE_ARRAY, ItemSerializer.serializeItemsToBytes(stored));
        changed = true;
      } 
    } 
    if (item.getAmount() > 1) {
      item.setAmount(1);
      changed = true;
    } 
    return changed;
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      if (isBlocked(player) || checkCooldown(player, item))
        return; 
      (new SakiewkaInventory(this.plugin)).open(player, item);
    } 
  }
  
  public void onEntityKill(EntityDeathEvent event, Player killer, Player victim, ItemStack item) {
    if (this.plugin.getTrybTworcyManager().hasModeEnabled(killer))
      return; 
    TotemUlaskawienia totem = (TotemUlaskawienia)this.plugin.getEventItemManager().getItemById("totem_ulaskawienia");
    if (totem != null && totem.hasUsedTotem(victim.getUniqueId()))
      return; 
    SakiewkaInventory sakiewka = new SakiewkaInventory(this.plugin);
    List<ItemStack> stored = sakiewka.getStoredItems(item);
    for (ItemStack drop : event.getDrops()) {
      if (drop != null && drop.getType() != Material.AIR)
        stored.add(drop); 
    } 
    event.getDrops().clear();
    sakiewka.saveItems(item, stored);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\SakiewkaDropu.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */