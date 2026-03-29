package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ZlamaneSerce extends EventItem {
  private int dur;
  
  private int slowness;
  
  private int slowFalling;
  
  public ZlamaneSerce(Main plugin) {
    super(plugin, "zlamane_serce");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.dur = this.plugin.getConfig().getInt("meta.zlamane_serce.duration_seconds", 8) * 20;
    this.slowness = this.plugin.getConfig().getInt("meta.zlamane_serce.slowness_level", 1) - 1;
    this.slowFalling = this.plugin.getConfig().getInt("meta.zlamane_serce.slow_falling_level", 1) - 1;
  }
  
  public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
    if (isBlocked(damager) || checkCooldown(damager, item))
      return; 
    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, this.dur, this.slowness));
    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, this.dur, this.slowFalling));
    sendVictimNotification(victim, damager);
    applyUse(damager, victim, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\ZlamaneSerce.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */