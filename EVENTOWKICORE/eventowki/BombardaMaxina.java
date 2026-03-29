package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class BombardaMaxina extends EventItem {
  private double explosionPower;
  
  public BombardaMaxina(Main plugin) {
    super(plugin, "bombarda_maxina");
  }
  
  public boolean isBlockedByCage() {
    return true;
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.explosionPower = this.plugin.getConfig().getDouble("meta.bombarda_maxina.explosion_power", 5.0D);
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      handleBombarda(player, item);
    } 
  }
  
  private void handleBombarda(Player player, ItemStack item) {
    if (isBlocked(player) || checkCooldown(player, item))
      return; 
    if (HydroKlatka.isInsideCage(player.getLocation())) {
      sendBlockedNotification(player);
      return;
    } 
    Fireball fireball = (Fireball)player.launchProjectile(Fireball.class);
    fireball.setYield(0.0F);
    fireball.setIsIncendiary(false);
    fireball.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
    applyUse(player, null, item, this.id);
  }
  
  public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
    Location loc = (event.getHitEntity() != null) ? event.getHitEntity().getLocation() : ((event.getHitBlock() != null) ? event.getHitBlock().getLocation() : event.getEntity().getLocation());
    if (HydroKlatka.isInsideCage(loc)) {
      if (shooter != null)
        sendBlockedNotification(shooter); 
      return;
    } 
    createUniformExplosion(loc);
  }
  
  private void createUniformExplosion(Location loc) {
    loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1);
    loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
    int radius = (int)this.explosionPower;
    double radiusSq = (radius * radius);
    for (int x = -radius; x <= radius; x++) {
      for (int y = -radius; y <= radius; y++) {
        for (int z = -radius; z <= radius; z++) {
          if ((x * x + y * y + z * z) <= radiusSq) {
            Block b = loc.clone().add(x, y, z).getBlock();
            if (b.getType() != Material.BEDROCK && b.getType() != Material.AIR)
              b.setType(Material.AIR); 
          } 
        } 
      } 
    } 
    loc.getWorld().createExplosion(loc, 0.0F, false, false);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\BombardaMaxina.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */