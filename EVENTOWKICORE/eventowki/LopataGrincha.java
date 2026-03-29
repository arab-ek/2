package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class LopataGrincha extends EventItem {
  public LopataGrincha(Main plugin) {
    super(plugin, "lopata_grincha");
  }
  
  public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
    if (isBlocked(damager) || checkCooldown(damager, item))
      return; 
    Location loc = victim.getLocation();
    loc.setYaw(this.plugin.getRandom().nextFloat() * 360.0F);
    loc.setPitch(this.plugin.getRandom().nextFloat() * 180.0F - 90.0F);
    victim.teleport(loc);
    victim.setFreezeTicks(100);
    sendVictimNotification(victim, damager);
    applyUse(damager, victim, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\LopataGrincha.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */