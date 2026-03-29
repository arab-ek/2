package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Kosa extends EventItem {
  private int effDur;
  
  public Kosa(Main plugin) {
    super(plugin, "kosa");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.effDur = this.plugin.getConfig().getInt("meta.kosa.effects_duration", 10) * 20;
  }
  
  public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
    if (isBlocked(damager) || checkCooldown(damager, item))
      return; 
    victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, this.effDur, 0));
    victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, this.effDur, 0));
    sendVictimNotification(victim, damager);
    applyUse(damager, victim, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\Kosa.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */