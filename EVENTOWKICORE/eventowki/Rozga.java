package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Rozga extends EventItem {
  public Rozga(Main plugin) {
    super(plugin, "rozga");
  }
  
  public boolean updateItem(ItemStack item, ItemMeta meta) {
    if (!meta.hasEnchant(Enchantment.KNOCKBACK) || meta.getEnchantLevel(Enchantment.KNOCKBACK) != 4) {
      meta.addEnchant(Enchantment.KNOCKBACK, 4, true);
      return true;
    } 
    return false;
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\Rozga.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */