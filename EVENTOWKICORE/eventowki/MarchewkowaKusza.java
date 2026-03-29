package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class MarchewkowaKusza extends EventItem {
  private double pullPower;
  
  public MarchewkowaKusza(Main plugin) {
    super(plugin, "marchewkowa_kusza");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.pullPower = this.plugin.getConfig().getDouble("meta.marchewkowa_kusza.pull_power", 5.0D);
  }
  
  public void onLaunch(ProjectileLaunchEvent event, Player player, ItemStack item) {
    Projectile projectile = event.getEntity();
    if (projectile instanceof Arrow) {
      Arrow arrow = (Arrow)projectile;
      if (isBlocked(player) || checkCooldown(player, item)) {
        event.setCancelled(true);
        return;
      } 
      arrow.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
      applyUseSilent(player, null, item, this.id);
    } 
  }
  
  public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
    if (shooter != null) {
      Entity entity = event.getHitEntity();
      if (entity instanceof Player) {
        Player victim = (Player)entity;
        Vector dir = shooter.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize();
        victim.setVelocity(dir.multiply(this.pullPower).setY(0.8D));
        sendUseNotification(shooter, victim);
        sendVictimNotification(victim, shooter);
      } 
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\MarchewkowaKusza.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */