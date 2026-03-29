package dev.arab.EVENTOWKICORE.inventory;

import java.util.ArrayList;
import java.util.List;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.EVENTOWKICORE.utils.ItemSerializer;
import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class SakiewkaInventory {
  private final Main plugin;
  
  private final NamespacedKey storageKey;
  
  public SakiewkaInventory(Main plugin) {
    this.plugin = plugin;
    this.storageKey = new NamespacedKey((Plugin)plugin, "sakiewka_contents");
  }
  
  public void open(Player player, ItemStack pouch) {
    String title = this.plugin.getMessagesConfig().getString("items.sakiewka_dropu.inventory_title", "     &8ꜱᴀᴋɪᴇᴡᴋᴀ ᴅʀᴏᴘᴜ");
    Inventory inv = Bukkit.createInventory(null, 54, ChatUtils.color(title));
    List<ItemStack> items = getStoredItems(pouch);
    for (int i = 0; i < Math.min(items.size(), 45); i++)
      inv.setItem(i, items.get(i)); 
    ItemStack receive = new ItemStack(Material.LIME_DYE);
    ItemMeta receiveMeta = receive.getItemMeta();
    String btnName = this.plugin.getMessagesConfig().getString("items.sakiewka_dropu.receive_button", "&aOdbierz wszystko");
    receiveMeta.setDisplayName(ChatUtils.color(btnName));
    receive.setItemMeta(receiveMeta);
    inv.setItem(49, receive);
    player.openInventory(inv);
  }
  
  public List<ItemStack> getStoredItems(ItemStack pouch) {
    ItemMeta meta = pouch.getItemMeta();
    if (meta == null)
      return new ArrayList<>(); 
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    if (pdc.has(this.storageKey, PersistentDataType.BYTE_ARRAY)) {
      byte[] bytes = (byte[])pdc.get(this.storageKey, PersistentDataType.BYTE_ARRAY);
      if (bytes != null)
        return ItemSerializer.deserializeItemsFromBytes(bytes); 
    } 
    if (pdc.has(this.storageKey, PersistentDataType.STRING)) {
      String data = (String)pdc.get(this.storageKey, PersistentDataType.STRING);
      if (data != null)
        return ItemSerializer.deserializeItems(data); 
    } 
    return new ArrayList<>();
  }
  
  public void saveItems(ItemStack pouch, List<ItemStack> items) {
    ItemMeta meta = pouch.getItemMeta();
    if (meta == null)
      return; 
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(this.storageKey, PersistentDataType.BYTE_ARRAY, ItemSerializer.serializeItemsToBytes(items));
    pouch.setItemMeta(meta);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\inventory\SakiewkaInventory.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */