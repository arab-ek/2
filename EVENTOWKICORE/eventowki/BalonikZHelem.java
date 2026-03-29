package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class BalonikZHelem extends EventItem {
    public BalonikZHelem(Main plugin) {
        super(plugin, "balonik_z_helem");
    }

    public boolean isBlockedByCage() {
        return true;
    }

    public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            this.handleBalonikZHelem(player, item, event.getClickedBlock().getLocation().add(0.5D, 1.0D, 0.5D));
        }

    }

    private void handleBalonikZHelem(Player player, ItemStack item, final Location loc) {
        if (!this.isBlocked(player)) {
            if (!HydroKlatka.isInsideCage(player.getLocation()) && !HydroKlatka.isInsideCage(loc)) {
                if (!this.checkCooldown(player, item)) {
                    final ArmorStand balloon = (ArmorStand)loc.getWorld().spawn(loc, ArmorStand.class, (as) -> {
                        as.setVisible(false);
                        as.setGravity(false);
                        as.setSmall(true);
                        as.setMarker(true);
                        as.getEquipment().setHelmet(item.clone());
                    });
                    (new BukkitRunnable() {
                        private final Location startLoc = loc.clone();
                        private float rotation = 0.0F;

                        public void run() {
                            Location cur = balloon.getLocation();
                            if (cur.getY() <= 320.0D && !balloon.isDead() && (double)cur.getWorld().getHighestBlockYAt(cur) >= cur.getY()) {
                                Location next = cur.add(0.0D, 0.8D, 0.0D);
                                balloon.teleport(next);
                                next.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, next.clone().add(0.0D, 0.2D, 0.0D), 3, 0.1D, 0.1D, 0.1D, 0.02D);
                                this.rotation += 10.0F;
                                balloon.setHeadPose(new EulerAngle(0.0D, Math.toRadians((double)this.rotation), 0.0D));
                                if (!BalonikZHelem.this.isLocationBlocked(next)) {
                                    this.breakBlock(next.getBlock());
                                    this.breakBlock(next.clone().add(0.0D, 1.0D, 0.0D).getBlock());
                                }

                            } else {
                                cur.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, cur.add(0.0D, 0.5D, 0.0D), 1);
                                cur.getWorld().playSound(cur, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                                balloon.remove();
                                this.cancel();
                            }
                        }

                        private void breakBlock(Block b) {
                            if (b.getType() != Material.AIR) {
                                if (b.getType() == Material.BEDROCK) {
                                    if (BalonikZHelem.this.plugin.getBlockTracker().isPlacedBlock(b.getLocation())) {
                                        b.breakNaturally();
                                    }
                                } else {
                                    b.breakNaturally();
                                }

                            }
                        }
                    }).runTaskTimer(this.plugin, 0L, 2L);
                    this.applyUse(player, (Player)null, item, this.id);
                }
            } else {
                this.sendBlockedNotification(player);
            }
        }
    }
}