/* Decompiler 23ms, total 204ms, lines 109 */
package dev.arab.EVENTOWKICORE.eventowki;

import java.util.Iterator;
import dev.arab.Main;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

public class PrzeterminowanyTrunek extends EventItem {
    double cloudRad;
    int cloudDur;
    int poisonLvl;
    int slownessLvl;

    public PrzeterminowanyTrunek(Main plugin) {
        super(plugin, "przeterminowany_trunek");
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.cloudRad = this.plugin.getConfig().getDouble("meta.przeterminowany_trunek.cloud_radius", 4.0D);
        this.cloudDur = this.plugin.getConfig().getInt("meta.przeterminowany_trunek.cloud_duration", 300);
        this.poisonLvl = this.plugin.getConfig().getInt("meta.przeterminowany_trunek.poison_level", 2);
        this.slownessLvl = this.plugin.getConfig().getInt("meta.przeterminowany_trunek.slowness_level", 1);
    }

    public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getAction().name().contains("RIGHT")) {
            event.setCancelled(true);
            if (this.isBlocked(player) || this.checkCooldown(player, item)) {
                return;
            }

            ThrownPotion potion = (ThrownPotion)player.launchProjectile(ThrownPotion.class);
            potion.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
            this.applyUse(player, (Player)null, item, this.id);
        }

    }

    public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
        Location loc = event.getHitEntity() != null ? event.getHitEntity().getLocation() : (event.getHitBlock() != null ? event.getHitBlock().getLocation() : event.getEntity().getLocation());
        this.handlePrzeterminowanyTrunekImpact(loc, shooter);
    }

    private void handlePrzeterminowanyTrunekImpact(Location loc, Player shooter) {
        final AreaEffectCloud cloud = (AreaEffectCloud)loc.getWorld().spawn(loc, AreaEffectCloud.class);
        cloud.setRadius((float)this.cloudRad);
        cloud.setDuration(this.cloudDur);
        cloud.setWaitTime(0);
        cloud.setColor(Color.GREEN);
        cloud.setBasePotionData(new PotionData(PotionType.WATER));
        (new BukkitRunnable() {
            int ticks = 0;

            public void run() {
                if (this.ticks <= PrzeterminowanyTrunek.this.cloudDur && !cloud.isDead()) {
                    Location cLoc = cloud.getLocation();
                    cloud.getWorld().spawnParticle(Particle.SPELL_MOB, cLoc, 60, PrzeterminowanyTrunek.this.cloudRad, 0.5D, PrzeterminowanyTrunek.this.cloudRad, 1.0D, Color.GREEN);
                    cloud.getWorld().spawnParticle(Particle.SLIME, cLoc, 20, PrzeterminowanyTrunek.this.cloudRad, 2.0D, 0.1D);
                    if (this.ticks % 20 == 0) {
                        Iterator var2 = cloud.getNearbyEntities(PrzeterminowanyTrunek.this.cloudRad, 2.0D, PrzeterminowanyTrunek.this.cloudRad).iterator();

                        while(var2.hasNext()) {
                            Entity entity = (Entity)var2.next();
                            if (entity instanceof Player) {
                                Player p = (Player)entity;
                                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, PrzeterminowanyTrunek.this.poisonLvl - 1));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, PrzeterminowanyTrunek.this.slownessLvl - 1));
                            }
                        }
                    }

                    this.ticks += 5;
                } else {
                    this.cancel();
                }
            }
        }).runTaskTimer(this.plugin, 0L, 5L);
        if (shooter != null) {
            Iterator var4 = loc.getWorld().getNearbyEntities(loc, this.cloudRad, this.cloudRad, this.cloudRad).iterator();

            while(var4.hasNext()) {
                Entity entity = (Entity)var4.next();
                if (entity instanceof Player) {
                    Player p = (Player)entity;
                    if (!p.equals(shooter)) {
                        this.sendVictimNotification(p, shooter);
                    }
                }
            }
        }

    }
}