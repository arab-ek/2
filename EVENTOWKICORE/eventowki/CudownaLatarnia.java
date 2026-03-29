/* Decompiler 42ms, total 237ms, lines 168 */
package dev.arab.EVENTOWKICORE.eventowki;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CudownaLatarnia extends EventItem {
    static final Map<Location, CudownaLatarnia.LanternInfo> activeLanterns = new HashMap();
    static final Map<Location, BossBar> activeBossBars = new HashMap();
    long durationMs;
    double radiusSq;
    int regen;
    int absorption;
    int strength;
    String destroyTitle;
    String destroySubtitle;

    public CudownaLatarnia(Main plugin) {
        super(plugin, "cudowna_latarnia");
    }

    public static void cleanupAll() {
        Iterator var0 = activeLanterns.keySet().iterator();

        while(var0.hasNext()) {
            Location loc = (Location)var0.next();
            loc.getBlock().setType(Material.AIR);
        }

        var0 = activeBossBars.values().iterator();

        while(var0.hasNext()) {
            BossBar bar = (BossBar)var0.next();
            bar.removeAll();
        }

        activeLanterns.clear();
        activeBossBars.clear();
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.durationMs = (long)this.plugin.getConfig().getInt("meta.cudowna_latarnia.active_duration", 30) * 1000L;
        double radius = this.plugin.getConfig().getDouble("meta.cudowna_latarnia.effect_radius", 30.0D);
        this.radiusSq = radius * radius;
        this.regen = this.plugin.getConfig().getInt("meta.cudowna_latarnia.regen_level", 5);
        this.absorption = this.plugin.getConfig().getInt("meta.cudowna_latarnia.absorption_level", 6);
        this.strength = this.plugin.getConfig().getInt("meta.cudowna_latarnia.strength_level", 2);
        this.destroyTitle = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.cudowna_latarnia.destroy_title", "&c&lZNISZCZONO"));
        this.destroySubtitle = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.cudowna_latarnia.destroy_subtitle", "&7Efekty latarni wygasly!"));
    }

    public void onInteract(PlayerInteractEvent event, final Player player, ItemStack item) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            if (!this.isBlocked(player) && !this.checkCooldown(player, item)) {
                final Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
                if (!activeLanterns.containsKey(loc)) {
                    if (loc.getBlock().getType().isAir() || loc.getBlock().isReplaceable()) {
                        if (loc.getWorld().getNearbyEntities(loc.clone().add(0.5D, 0.5D, 0.5D), 0.5D, 0.5D, 0.5D, (e) -> {
                            return e instanceof LivingEntity;
                        }).isEmpty()) {
                            this.applyUse(player, (Player)null, item, this.id);
                            loc.getBlock().setType(Material.BEACON);
                            player.swingMainHand();
                            activeLanterns.put(loc.getBlock().getLocation(), new CudownaLatarnia.LanternInfo(player.getUniqueId(), System.currentTimeMillis() + this.durationMs));
                            loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.0F);
                            loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0.5D, 0.5D, 0.5D), 50, 0.5D, 0.5D, 0.5D, 0.1D);
                            final BossBar bossBar = Bukkit.createBossBar(ChatUtils.color("&d&lCudowna Latarnia"), BarColor.PINK, BarStyle.SOLID, new BarFlag[0]);
                            bossBar.setVisible(true);
                            bossBar.addPlayer(player);
                            activeBossBars.put(loc, bossBar);
                            (new BukkitRunnable() {
                                public void run() {
                                    long now = System.currentTimeMillis();
                                    CudownaLatarnia.LanternInfo info = (CudownaLatarnia.LanternInfo)CudownaLatarnia.activeLanterns.get(loc);
                                    Block block = loc.getBlock();
                                    Material type = block.getType();
                                    if (info != null && now < info.expiry && type == Material.BEACON) {
                                        bossBar.setProgress(Math.max(0.0D, (double)(info.expiry - now) / (double)CudownaLatarnia.this.durationMs));
                                        if (player.isOnline()) {
                                            Location centerPos = loc.clone().add(0.5D, 0.5D, 0.5D);
                                            if (player.getWorld().equals(loc.getWorld()) && player.getLocation().distanceSquared(centerPos) <= CudownaLatarnia.this.radiusSq) {
                                                if (!bossBar.getPlayers().contains(player)) {
                                                    bossBar.addPlayer(player);
                                                }

                                                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, CudownaLatarnia.this.regen - 1));
                                                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 40, CudownaLatarnia.this.absorption - 1));
                                                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, CudownaLatarnia.this.strength - 1));
                                            } else {
                                                bossBar.removePlayer(player);
                                            }

                                        }
                                    } else {
                                        if (type == Material.BEACON) {
                                            block.setType(Material.AIR);
                                        }

                                        CudownaLatarnia.activeLanterns.remove(loc.getBlock().getLocation());
                                        BossBar bar = (BossBar)CudownaLatarnia.activeBossBars.remove(loc.getBlock().getLocation());
                                        if (bar != null) {
                                            bar.removeAll();
                                        }

                                        this.cancel();
                                        if (player.isOnline()) {
                                            player.sendTitle(CudownaLatarnia.this.destroyTitle, CudownaLatarnia.this.destroySubtitle, 5, 20, 5);
                                        }

                                    }
                                }
                            }).runTaskTimer(this.plugin, 0L, 20L);
                        }
                    }
                }
            }
        }
    }

    public void onBlockBreak(BlockBreakEvent event, Player player) {
        Location loc = event.getBlock().getLocation();
        CudownaLatarnia.LanternInfo info = (CudownaLatarnia.LanternInfo)activeLanterns.get(loc);
        if (info != null) {
            event.setDropItems(false);
            activeLanterns.remove(loc);
            BossBar bar = (BossBar)activeBossBars.remove(loc);
            if (bar != null) {
                bar.removeAll();
            }
        }

    }

    static class LanternInfo {
        UUID ownerUUID;
        long expiry;

        LanternInfo(UUID ownerUUID, long expiry) {
            this.ownerUUID = ownerUUID;
            this.expiry = expiry;
        }
    }
}