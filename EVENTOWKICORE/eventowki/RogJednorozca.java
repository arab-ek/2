/* Decompiler 113ms, total 364ms, lines 131 */
package dev.arab.EVENTOWKICORE.eventowki;

import java.util.Iterator;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse.Color;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class RogJednorozca extends EventItem {
    private int rideDur;
    int stunDur;
    String stunTitle;
    String stunSub;

    public RogJednorozca(Main plugin) {
        super(plugin, "rog_jednorozca");
    }

    public boolean isBlockedByCage() {
        return true;
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.rideDur = this.plugin.getConfig().getInt("meta.rog_jednorozca.ride_duration", 30) * 20;
        this.stunDur = this.plugin.getConfig().getInt("meta.rog_jednorozca.stun_duration", 5) * 20;
        this.stunTitle = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.rog_jednorozca.stun_victim_title"));
        this.stunSub = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.rog_jednorozca.stun_victim_subtitle"));
    }

    public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getAction().name().contains("RIGHT")) {
            event.setCancelled(true);
            this.handleRogJednorozca(player, item);
        }

    }

    private void handleRogJednorozca(final Player player, ItemStack item) {
        if (!this.isBlocked(player) && !this.checkCooldown(player, item)) {
            if (HydroKlatka.isInsideCage(player.getLocation())) {
                this.sendBlockedNotification(player);
            } else {
                Location loc = player.getLocation();
                final Horse unicorn = (Horse)loc.getWorld().spawn(loc, Horse.class);
                unicorn.setColor(Color.WHITE);
                unicorn.setTamed(true);
                unicorn.setOwner(player);
                unicorn.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                unicorn.setJumpStrength(1.0D);
                unicorn.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.7D);
                unicorn.addPassenger(player);
                (new BukkitRunnable() {
                    int ticksLeft = 200;
                    int counter = 0;

                    public void run() {
                        if (this.ticksLeft-- > 0 && !unicorn.isDead() && !unicorn.getPassengers().isEmpty()) {
                            ++this.counter;
                            if (this.counter % 2 == 0) {
                                Vector dir = unicorn.getLocation().getDirection().setY(0).normalize();
                                Location front = unicorn.getLocation().add(dir.clone().multiply(1.5D));
                                Vector side = new Vector(-dir.getZ(), 0.0D, dir.getX());

                                for(int dWidth = -1; dWidth <= 1; ++dWidth) {
                                    for(int dHeight = 0; dHeight <= 3; ++dHeight) {
                                        Block b = front.clone().add(side.clone().multiply(dWidth)).add(0.0D, (double)dHeight, 0.0D).getBlock();
                                        Material type = b.getType();
                                        if (type != Material.AIR) {
                                            if (type == Material.BEDROCK) {
                                                if (RogJednorozca.this.plugin.getBlockTracker().isPlacedBlock(b.getLocation())) {
                                                    b.setType(Material.AIR, false);
                                                }
                                            } else {
                                                b.setType(Material.AIR, false);
                                            }
                                        }
                                    }
                                }
                            }

                            if (this.counter % 5 == 0) {
                                Iterator var8 = unicorn.getNearbyEntities(1.5D, 1.5D, 1.5D).iterator();

                                while(true) {
                                    Player v;
                                    do {
                                        do {
                                            Entity e;
                                            do {
                                                if (!var8.hasNext()) {
                                                    return;
                                                }

                                                e = (Entity)var8.next();
                                            } while(!(e instanceof Player));

                                            v = (Player)e;
                                        } while(v == player);

                                        v.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, RogJednorozca.this.stunDur, 2));
                                        v.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, RogJednorozca.this.stunDur, 0));
                                    } while(RogJednorozca.this.stunTitle == null && RogJednorozca.this.stunSub == null);

                                    v.sendTitle(RogJednorozca.this.stunTitle, RogJednorozca.this.stunSub, 5, 20, 5);
                                }
                            }
                        } else {
                            unicorn.remove();
                            this.cancel();
                        }
                    }
                }).runTaskTimer(this.plugin, 0L, 1L);
                this.applyUse(player, (Player)null, item, this.id);
            }
        }
    }
}