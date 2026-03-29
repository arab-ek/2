/* Decompiler 37ms, total 536ms, lines 74 */
package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class LeweJajko extends EventItem {
  private double launchPower;

  public LeweJajko(Main plugin) {
    super(plugin, "lewe_jajko");
  }

  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.launchPower = this.plugin.getConfig().getDouble("meta.lewe_jajko.launch_power", 1.5D);
  }

  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      if (this.isBlocked(player) || this.checkCooldown(player, item)) {
        return;
      }

      Egg egg = (Egg)player.launchProjectile(Egg.class);
      egg.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
      this.applyUse(player, (Player)null, item, this.id);
    }

  }

  public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
    if (!this.isBlocked(damager) && !this.checkCooldown(damager, item)) {
      this.executeLaunch(victim);
      this.sendVictimNotification(victim, damager);
      this.applyUse(damager, victim, item, this.id);
    }
  }

  public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
    if (shooter != null) {
      Entity var4 = event.getHitEntity();
      if (var4 instanceof Player) {
        Player victim = (Player)var4;
        this.executeLaunch(victim);
        this.sendVictimNotification(victim, shooter);
      }
    }

  }

  private void executeLaunch(Player victim) {
    if (victim != null && victim.getGameMode() != GameMode.CREATIVE && victim.getGameMode() != GameMode.SPECTATOR) {
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
        if (victim.isOnline()) {
          victim.teleport(victim.getLocation().add(0.0D, 0.1D, 0.0D));
          victim.setVelocity(new Vector(0.0D, this.launchPower, 0.0D));
        }

      }, 1L);
    }
  }
}