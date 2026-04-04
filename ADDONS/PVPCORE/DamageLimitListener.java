package dev.arab.ADDONS.PVPCORE;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageLimitListener implements Listener {
  private final ConfigPvpCore config;
  
  public DamageLimitListener(ConfigPvpCore config) {
    this.config = config;
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof org.bukkit.entity.Player))
      return; 
    double limit = this.config.getDamageLimit();
    if (event.getDamage() > limit)
      event.setDamage(limit); 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\PVPCORE\DamageLimitListener.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */