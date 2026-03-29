package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SmoczyMiecz extends EventItem {
  public SmoczyMiecz(Main plugin) {
    super(plugin, "smoczy_miecz");
  }
  
  public void onGive(Player player, ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.addEnchant(Enchantment.DAMAGE_ALL, 6, true);
      meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
      meta.addEnchant(Enchantment.DURABILITY, 3, true);
      applyHideFlags(meta);
      item.setItemMeta(meta);
    } 
  }
  
  public boolean updateItem(ItemStack item, ItemMeta meta) {
    boolean changed = false;
    if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) < 6) {
      meta.addEnchant(Enchantment.DAMAGE_ALL, 6, true);
      changed = true;
    } 
    if (meta.getEnchantLevel(Enchantment.FIRE_ASPECT) < 2) {
      meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
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
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      handleSmoczyMiecz(player, item);
    } 
  }
  
  private void handleSmoczyMiecz(Player player, ItemStack item) {
    if (isBlocked(player) || checkCooldown(player, item))
      return; 
    player.launchProjectile(EnderPearl.class);
    applyUse(player, null, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\SmoczyMiecz.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */