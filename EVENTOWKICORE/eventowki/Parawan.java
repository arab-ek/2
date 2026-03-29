package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Parawan extends EventItem {
  private double kbRad;
  
  private double kbPow;
  
  public Parawan(Main plugin) {
    super(plugin, "parawan");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.kbRad = this.plugin.getConfig().getDouble("meta.parawan.knockback_radius", 5.0D);
    this.kbPow = this.plugin.getConfig().getDouble("meta.parawan.knockback_power", 1.5D);
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      handleParawan(player, item);
    } 
  }
  
  private void handleParawan(Player player, ItemStack item) {
    if (isBlocked(player) || checkCooldown(player, item))
      return; 
    Location center = player.getLocation();
    for (Entity entity : player.getNearbyEntities(this.kbRad, this.kbRad, this.kbRad)) {
      if (entity instanceof Player) {
        Player victim = (Player)entity;
        if (!victim.equals(player)) {
          Vector push = victim.getLocation().toVector().subtract(center.toVector()).normalize().setY(0.5D);
          victim.setVelocity(push.multiply(this.kbPow));
          sendVictimNotification(victim, player);
        } 
      } 
    } 
    applyUse(player, null, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\Parawan.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */