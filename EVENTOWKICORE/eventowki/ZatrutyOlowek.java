package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ZatrutyOlowek extends EventItem {
  private double chance;
  
  private int dur;
  
  private int level;
  
  public ZatrutyOlowek(Main plugin) {
    super(plugin, "zatruty_olowek");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.chance = this.plugin.getConfig().getDouble("meta.zatruty_olowek.poison_chance", 25.0D);
    this.dur = this.plugin.getConfig().getInt("meta.zatruty_olowek.poison_duration", 5) * 20;
    this.level = this.plugin.getConfig().getInt("meta.zatruty_olowek.poison_level", 2) - 1;
  }
  
  public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
    if (isBlocked(damager) || checkCooldown(damager, item))
      return; 
    if (this.plugin.getRandom().nextDouble() * 100.0D <= this.chance) {
      victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, this.dur, this.level));
      sendVictimNotification(victim, damager);
      applyUse(damager, victim, item, this.id);
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\ZatrutyOlowek.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */