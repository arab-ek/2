package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class SiekieraGrincha extends EventItem {
  public SiekieraGrincha(Main plugin) {
    super(plugin, "siekiera_grincha");
  }
  
  public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
    if (isBlocked(damager) || checkCooldown(damager, item))
      return; 
    victim.getWorld().strikeLightningEffect(victim.getLocation());
    double dmg = Math.max(victim.getHealth() * 0.3D, 1.0D);
    victim.setHealth(Math.max(victim.getHealth() - dmg, 0.0D));
    sendVictimNotification(victim, damager);
    applyUse(damager, victim, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\SiekieraGrincha.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */