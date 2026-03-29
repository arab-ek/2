package dev.arab.EVENTOWKICORE.eventowki;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import dev.arab.Main;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ZajeczyMiecz extends EventItem {
  private int jumpDur;
  
  public ZajeczyMiecz(Main plugin) {
    super(plugin, "zajeczy_miecz");
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
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.jumpDur = this.plugin.getConfig().getInt("meta.zajeczy_miecz.jump_blocked_duration", 4);
  }
  
  public void onJump(PlayerJumpEvent event, Player player) {
    if (this.plugin.getCooldownManager().getRemainingMs(player.getUniqueId(), "zajeczy_miecz_jump_blocked") > 0L)
      event.setCancelled(true); 
  }
  
  public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
    if (isBlocked(damager) || checkCooldown(damager, item))
      return; 
    this.plugin.getCooldownManager().setCooldown(victim.getUniqueId(), "zajeczy_miecz_jump_blocked", this.jumpDur);
    sendVictimNotification(victim, damager);
    applyUse(damager, victim, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\ZajeczyMiecz.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */