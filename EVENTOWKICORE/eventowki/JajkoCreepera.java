package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class JajkoCreepera extends EventItem {
  private double radius;
  
  private double damage;
  
  public JajkoCreepera(Main plugin) {
    super(plugin, "jajko_creepera");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.radius = this.plugin.getConfig().getDouble("meta.jajko_creepera.radius", 5.0D);
    this.damage = this.plugin.getConfig().getDouble("meta.jajko_creepera.damage", 9.0D);
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      handleJajkoCreepera(player, item);
    } 
  }
  
  public void onEntityExplode(EntityExplodeEvent event) {
    Entity entity = event.getEntity();
    if (entity instanceof Creeper) {
      Creeper creeper = (Creeper)entity;
      if (creeper.getPersistentDataContainer().has(this.itemKey, PersistentDataType.STRING)) {
        event.setCancelled(true);
        Location loc = creeper.getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 2);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.2F, 1.0F);
        for (Entity e : loc.getWorld().getNearbyEntities(loc, this.radius, this.radius, this.radius)) {
          if (e instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)e;
            living.damage(this.damage, (Entity)creeper);
          } 
        } 
        creeper.remove();
        return;
      } 
    } 
  }
  
  private void handleJajkoCreepera(Player player, ItemStack item) {
    if (isBlocked(player) || checkCooldown(player, item))
      return; 
    Creeper creeper = (Creeper)player.getWorld().spawn(player.getLocation(), Creeper.class);
    creeper.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
    creeper.setPowered(true);
    creeper.setExplosionRadius(0);
    creeper.setMaxFuseTicks(60);
    creeper.setFuseTicks(0);
    creeper.ignite();
    applyUse(player, null, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\JajkoCreepera.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */