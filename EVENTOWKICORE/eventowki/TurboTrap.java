/* Decompiler 107ms, total 333ms, lines 209 */
package dev.arab.EVENTOWKICORE.eventowki;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class TurboTrap extends EventItem {
    static final List<BukkitRunnable> activeTasks = new CopyOnWriteArrayList();
    private List<String> cachedBlocks;
    private String trapSubtitle;

    public TurboTrap(Main plugin) {
        super(plugin, "turbo_trap");
    }

    public boolean isBlockedByCage() {
        return true;
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.cachedBlocks = this.plugin.getConfig().getStringList("meta.turbo_trap.blocks");
        this.trapSubtitle = this.plugin.getMessagesConfig().getString("items.turbo_trap.use_subtitle");
    }

    public static void resetAll() {
        Iterator var0 = activeTasks.iterator();

        while(var0.hasNext()) {
            BukkitRunnable task = (BukkitRunnable)var0.next();

            try {
                task.cancel();
            } catch (Exception var3) {
            }
        }

        activeTasks.clear();
    }

    private void registerTask(BukkitRunnable task) {
        activeTasks.add(task);
        task.runTaskTimer(this.plugin, 0L, 2L);
    }

    public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getAction().name().contains("RIGHT")) {
            event.setCancelled(true);
            this.handleTurboTrap(player, item);
        }

    }

    public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
        Location loc = event.getEntity().getLocation();
        if (event.getHitBlock() != null) {
            loc = event.getHitBlock().getLocation();
        } else if (event.getHitEntity() != null) {
            loc = event.getHitEntity().getLocation();
        }

        if (HydroKlatka.isInsideCage(loc)) {
            if (shooter != null) {
                this.sendBlockedNotification(shooter);
            }

        } else {
            this.buildTrap(loc, shooter);
        }
    }

    private void handleTurboTrap(Player player, ItemStack item) {
        if (!this.isBlocked(player) && !this.checkCooldown(player, item)) {
            if (HydroKlatka.isInsideCage(player.getLocation())) {
                this.sendBlockedNotification(player);
            } else {
                final Egg egg = (Egg)player.launchProjectile(Egg.class);
                egg.setMetadata("custom_item", new FixedMetadataValue(this.plugin, this.id));
                BukkitRunnable eggTask = new BukkitRunnable() {
                    public void run() {
                        if (!egg.isDead() && egg.isValid()) {
                            egg.getWorld().spawnParticle(Particle.PORTAL, egg.getLocation(), 5, 0.1D, 0.1D, 0.1D, 0.05D);
                        } else {
                            this.cancel();
                            TurboTrap.activeTasks.removeIf((task) -> {
                                return task == this;
                            });
                        }
                    }
                };
                this.registerTask(eggTask);
                this.applyUse(player, (Player)null, item, this.id);
            }
        }
    }

    private void buildTrap(Location loc, Player player) {
        if (this.cachedBlocks != null && !this.cachedBlocks.isEmpty()) {
            BlockFace facing = player != null ? player.getFacing() : BlockFace.NORTH;
            if (player != null && this.trapSubtitle != null) {
                player.sendTitle("", ChatUtils.color(this.trapSubtitle), 10, 60, 10);
            }

            final TreeMap<Integer, List<TurboTrap.TrapBlock>> layers = new TreeMap();
            Iterator var5 = this.cachedBlocks.iterator();

            while(var5.hasNext()) {
                String s = (String)var5.next();
                String[] parts = s.split(":");
                if (parts.length >= 4) {
                    Material mat = Material.matchMaterial(parts[0]);
                    if (mat != null) {
                        try {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            int z = Integer.parseInt(parts[3]);
                            int rx = x;
                            int rz = z;
                            switch(facing) {
                                case EAST:
                                    rx = -z;
                                    rz = x;
                                    break;
                                case SOUTH:
                                    rx = -x;
                                    rz = -z;
                                    break;
                                case WEST:
                                    rx = z;
                                    rz = -x;
                            }

                            Location target = loc.clone().add((double)rx, (double)y, (double)rz);
                            ((List)layers.computeIfAbsent(y, (k) -> {
                                return new ArrayList();
                            })).add(new TurboTrap.TrapBlock(target, mat, parts));
                        } catch (NumberFormatException var15) {
                        }
                    }
                }
            }

            BukkitRunnable buildTask = new BukkitRunnable() {
                private final Iterator<Integer> layerIterator = layers.descendingKeySet().iterator();

                public void run() {
                    if (!this.layerIterator.hasNext()) {
                        this.cancel();
                        TurboTrap.activeTasks.removeIf((task) -> {
                            return task == this;
                        });
                    } else {
                        int y = (Integer)this.layerIterator.next();
                        Iterator var2 = ((List)layers.get(y)).iterator();

                        while(var2.hasNext()) {
                            TurboTrap.TrapBlock tb = (TurboTrap.TrapBlock)var2.next();
                            Block b = tb.loc.getBlock();
                            if (b.getType() != Material.BEDROCK && b.getType() != Material.BARRIER) {
                                b.setType(tb.mat);
                                if (tb.parts.length >= 5 && tb.mat.name().contains("TRAPDOOR")) {
                                    BlockData var6 = b.getBlockData();
                                    if (var6 instanceof TrapDoor) {
                                        TrapDoor td = (TrapDoor)var6;
                                        td.setOpen(false);
                                        b.setBlockData(td);
                                    }
                                }
                            }
                        }

                    }
                }
            };
            this.registerTask(buildTask);
        }
    }

    static class TrapBlock {
        final Location loc;
        final Material mat;
        final String[] parts;

        TrapBlock(Location loc, Material mat, String[] parts) {
            this.loc = loc;
            this.mat = mat;
            this.parts = parts;
        }
    }
}