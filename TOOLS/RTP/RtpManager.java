package dev.arab.TOOLS.RTP;

import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RtpManager {
    private final Main plugin;
    private final ConfigRtp config;
    private final Random random;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public RtpManager(Main plugin, ConfigRtp config) {
        this.plugin = plugin;
        this.config = config;
        this.random = new Random();
    }

    // Główna logika teleportacji RTP. EnforceCooldown = true dla komendy, false np. dla przycisku.
    public void performRtp(Player player, boolean enforceCooldown) {
        UUID uuid = player.getUniqueId();

        // System Cooldownu
        if (enforceCooldown) {
            int cooldownSeconds = config.getConfig().getInt("settings.cooldown_seconds", 5);
            if (cooldowns.containsKey(uuid)) {
                long timeLeft = (cooldowns.get(uuid) - System.currentTimeMillis()) / 1000;
                if (timeLeft > 0) {
                    player.sendMessage(config.getMessage("cooldown").replace("{TIME}", String.valueOf(timeLeft)));
                    return;
                }
            }
            // Zapisujemy czas użycia komendy
            cooldowns.put(uuid, System.currentTimeMillis() + (cooldownSeconds * 1000L));
        }

        player.sendMessage(config.getMessage("teleporting"));

        String worldName = config.getConfig().getString("settings.teleport_world", "world");
        World targetWorld = Bukkit.getWorld(worldName);
        if (targetWorld == null) {
            targetWorld = player.getWorld();
            plugin.getLogger().warning("Świat RTP '" + worldName + "' nie istnieje! Używam obecnego świata gracza.");
        }

        final World finalWorld = targetWorld;

        // Szukamy lokacji asynchronicznie
        new BukkitRunnable() {
            @Override
            public void run() {
                Location safeLocation = findSafeLocation(finalWorld);

                if (safeLocation != null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(safeLocation);
                            player.sendMessage(config.getMessage("success")
                                    .replace("{X}", String.valueOf(safeLocation.getBlockX()))
                                    .replace("{Y}", String.valueOf(safeLocation.getBlockY()))
                                    .replace("{Z}", String.valueOf(safeLocation.getBlockZ())));
                        }
                    }.runTask(plugin);
                } else {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(config.getMessage("failed"));
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private Location findSafeLocation(World world) {
        int min = config.getConfig().getInt("settings.min_radius", 100);
        int max = config.getConfig().getInt("settings.max_radius", 3000);
        int maxAttempts = config.getConfig().getInt("settings.max_attempts", 10);

        WorldBorder border = world.getWorldBorder();
        double borderSize = border.getSize() / 2.0;
        double borderX = border.getCenter().getX();
        double borderZ = border.getCenter().getZ();

        for (int i = 0; i < maxAttempts; i++) {
            int x = random.nextInt((max - min) + 1) + min;
            int z = random.nextInt((max - min) + 1) + min;

            if (random.nextBoolean()) x = -x;
            if (random.nextBoolean()) z = -z;

            // Zabezpieczenie przed borderem mapy
            if (Math.abs(x - borderX) > borderSize || Math.abs(z - borderZ) > borderSize) {
                continue;
            }

            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x, y, z);
            Block highestBlock = loc.getBlock();

            if (isSafe(highestBlock.getType())) {
                return loc.add(0.5, 1.0, 0.5);
            }
        }
        return null;
    }

    private boolean isSafe(Material material) {
        switch (material) {
            case WATER:
            case LAVA:
            case CACTUS:
            case MAGMA_BLOCK:
            case FIRE:
            case AIR:
            case OAK_LEAVES:
            case BIRCH_LEAVES:
            case SPRUCE_LEAVES:
            case JUNGLE_LEAVES:
            case ACACIA_LEAVES:
            case DARK_OAK_LEAVES:
                return false;
            default:
                return true;
        }
    }
}