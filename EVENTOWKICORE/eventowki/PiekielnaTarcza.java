package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class PiekielnaTarcza extends EventItem {
  public PiekielnaTarcza(Main plugin) {
    super(plugin, "piekielna_tarcza");
  }
  
  public void onDamageVictim(EntityDamageByEntityEvent event, Player victim, Player damager, ItemStack item) {
    if (damager == null || isBlocked(victim) || checkCooldown(victim, item))
      return; 
    if (victim.isBlocking()) {
      damager.damage(event.getDamage(), (Entity)victim);
      applyUse(victim, damager, item, this.id);
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\PiekielnaTarcza.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */