package dev.arab.TOOLS.PROTECTION;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Iterator;

public class BorderController implements Listener {
    private final ConfigProtection config;

    public BorderController(ConfigProtection config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void handlePlace(BlockPlaceEvent event) {
        if (!config.getConfig().getBoolean("settings.border.enabled", true)) return;
        if (isNearBorder(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void handleBreak(BlockBreakEvent event) {
        if (!config.getConfig().getBoolean("settings.border.enabled", true)) return;
        if (isNearBorder(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleMove(PlayerMoveEvent event) {
        if (!config.getConfig().getBoolean("settings.border.enabled", true)) return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        if (world.getName().equals(config.getConfig().getString("settings.border.world", "world"))) {
            WorldBorder border = world.getWorldBorder();
            Location loc = player.getLocation();
            double borderSize = border.getSize() / 2.0D;
            Location center = border.getCenter();

            double minX = center.getX() - borderSize;
            double maxX = center.getX() + borderSize;
            double minZ = center.getZ() - borderSize;
            double maxZ = center.getZ() + borderSize;

            if (loc.getX() < minX || loc.getX() > maxX || loc.getZ() < minZ || loc.getZ() > maxZ) {
                Vector knockback = center.toVector().subtract(loc.toVector());
                knockback.setY(0);
                knockback.normalize().multiply(1.5D).setY(0.4D);
                player.setVelocity(knockback);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handlePlaceVehicle(PlayerInteractEvent event) {
        if (!config.getConfig().getBoolean("settings.border.enabled", true)) return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() != Material.AIR && (item.getType().toString().contains("BOAT") || item.getType() == Material.MINECART)) {
                if (isNearBorder(player.getLocation())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handlePearl(ProjectileLaunchEvent event) {
        if (!config.getConfig().getBoolean("settings.border.enabled", true)) return;
        Projectile proj = event.getEntity();
        if (proj instanceof EnderPearl) {
            ProjectileSource shooter = proj.getShooter();
            if (shooter instanceof Player) {
                Player player = (Player) shooter;
                if (isNearBorder(player.getLocation())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void handlePistone(BlockPistonExtendEvent event) {
        if (!config.getConfig().getBoolean("settings.border.enabled", true)) return;

        Iterator<Block> iter = event.getBlocks().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (isNearBorder(block.getLocation())) {
                event.setCancelled(true);
                block.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, event.getBlock().getLocation().toCenterLocation(), 30);
                block.setType(Material.AIR);
                block.getWorld().playSound(block.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0F, 1.0F);
                break;
            }
        }
    }

    private boolean isNearBorder(Location location) {
        World world = Bukkit.getWorld(config.getConfig().getString("settings.border.world", "world"));
        if (world != null && location.getWorld().equals(world)) {
            WorldBorder border = world.getWorldBorder();
            double borderSize = border.getSize() / 2.0D;
            double centerX = border.getCenter().getX();
            double centerZ = border.getCenter().getZ();
            double distanceX = Math.abs(location.getX() - centerX);
            double distanceZ = Math.abs(location.getZ() - centerZ);

            double radius = config.getConfig().getDouble("settings.border.protection_radius", 10.0);
            return (borderSize - distanceX <= radius || borderSize - distanceZ <= radius);
        }
        return false;
    }
}