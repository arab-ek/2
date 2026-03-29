package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KoronaAnarchii extends EventItem {
  public KoronaAnarchii(Main plugin) {
    super(plugin, "korona_anarchii");
  }
  
  public void onGive(Player player, ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);
      meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
      item.setItemMeta(meta);
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\KoronaAnarchii.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */