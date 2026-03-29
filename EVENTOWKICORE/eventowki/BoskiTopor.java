/* Decompiler 47ms, total 261ms, lines 101 */
package dev.arab.EVENTOWKICORE.eventowki;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ScoreboardUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BoskiTopor extends EventItem {
    static final Set<UUID> immortalityActive = ConcurrentHashMap.newKeySet();
    int immDur;
    private double shockRad;
    private double shockPow;

    public BoskiTopor(Main plugin) {
        super(plugin, "boski_topor");
    }

    public static boolean isImmortal(UUID uuid) {
        return immortalityActive.contains(uuid);
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.immDur = this.plugin.getConfig().getInt("meta.boski_topor.immortality_duration", 5) * 20;
        this.shockRad = this.plugin.getConfig().getDouble("meta.boski_topor.shockwave_radius", 8.0D);
        this.shockPow = this.plugin.getConfig().getDouble("meta.boski_topor.shockwave_power", 2.0D);
    }

    public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getAction().name().contains("RIGHT")) {
            event.setCancelled(true);
            this.handleBoskiTopor(player, item);
        }

    }

    private void handleBoskiTopor(final Player player, ItemStack item) {
        if (!this.isBlocked(player) && !this.checkCooldown(player, item)) {
            immortalityActive.add(player.getUniqueId());
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, this.immDur, 254));
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, this.immDur, 0));
            (new BukkitRunnable() {
                int ticks = 0;

                public void run() {
                    if (this.ticks < BoskiTopor.this.immDur && player.isOnline()) {
                        if (this.ticks % 3 == 0) {
                            ChatColor[] colors = new ChatColor[]{ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW};
                            ChatColor color = colors[this.ticks / 3 % colors.length];
                            ScoreboardUtils.applyRgbTeam(player, color);
                        }

                        ++this.ticks;
                    } else {
                        BoskiTopor.immortalityActive.remove(player.getUniqueId());
                        ScoreboardUtils.removeFromRgbTeams(player);
                        if (player.isOnline()) {
                            player.setGlowing(false);
                        }

                        this.cancel();
                    }
                }
            }).runTaskTimer(this.plugin, 0L, 1L);
            Location center = player.getLocation();
            player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0F, 0.8F);
            player.getWorld().playSound(center, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.5F, 1.2F);
            center.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, center, 1, 0.5D, 0.5D, 0.5D, 0.1D);
            Iterator var4 = player.getNearbyEntities(this.shockRad, this.shockRad, this.shockRad).iterator();

            while(var4.hasNext()) {
                Entity entity = (Entity)var4.next();
                if (entity instanceof Player && !entity.equals(player)) {
                    Player target = (Player)entity;
                    if (!this.isLocationBlocked(target.getLocation())) {
                        Vector dir = target.getLocation().toVector().subtract(center.toVector()).normalize().setY(0.5D);
                        target.setVelocity(dir.multiply(this.shockPow));
                        this.sendVictimNotification(target, player);
                    }
                }
            }

            this.applyUse(player, (Player)null, item, this.id);
        }
    }
}