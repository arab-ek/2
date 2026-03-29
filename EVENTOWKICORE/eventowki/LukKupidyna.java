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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LukKupidyna extends EventItem {
  private int durationTicks;
  
  public LukKupidyna(Main plugin) {
    super(plugin, "luk_kupidyna");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.durationTicks = this.plugin.getConfig().getInt("meta.luk_kupidyna.duration", 5) * 20;
  }
  
  public void onLaunch(ProjectileLaunchEvent event, Player player, ItemStack item) {
    Projectile projectile = event.getEntity();
    if (projectile instanceof Arrow) {
      Arrow arrow = (Arrow)projectile;
      if (checkCooldown(player, item)) {
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
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, this.durationTicks, 0));
        sendVictimNotification(victim, shooter);
      } 
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\LukKupidyna.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */