package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class Sniezka extends EventItem {
  private boolean limitEnabled;
  
  private double limitSq;
  
  public Sniezka(Main plugin) {
    super(plugin, "sniezka");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.limitEnabled = this.plugin.getConfig().getBoolean("meta.sniezka.distance_limit_enabled", true);
    double limit = this.plugin.getConfig().getDouble("meta.sniezka.distance_limit", 20.0D);
    this.limitSq = limit * limit;
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      handleSniezka(player, item);
    } 
  }
  
  public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
    if (shooter != null) {
      Entity entity = event.getHitEntity();
      if (entity instanceof Player) {
        Player victim = (Player)entity;
        if (this.limitEnabled && (!shooter.getWorld().equals(victim.getWorld()) || shooter.getLocation().distanceSquared(victim.getLocation()) > this.limitSq))
          return; 
        Location sLoc = shooter.getLocation().clone();
        Location vLoc = victim.getLocation().clone();
        shooter.teleport(vLoc);
        victim.teleport(sLoc);
        sendUseNotification(shooter, victim);
        sendVictimNotification(victim, shooter);
      } 
    } 
  }
  
  private void handleSniezka(Player player, ItemStack item) {
    if (isBlocked(player) || checkCooldown(player, item))
      return; 
    Snowball s = (Snowball)player.launchProjectile(Snowball.class);
    s.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
    applyUseSilent(player, null, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\Sniezka.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */