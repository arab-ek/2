package dev.arab.TOOLS.CLEANER;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CleanerController implements Listener {
    private final Main plugin;
    private final ConfigCleaner config;
    private final Map<Location, Long> temporaryBlocks = new ConcurrentHashMap<>();
    private RegionQuery regionQuery;

    public CleanerController(Main plugin, ConfigCleaner config) {
        this.plugin = plugin;
        this.config = config;

        try {
            this.regionQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        } catch (NoClassDefFoundError ignored) {
            plugin.getLogger().warning("WorldGuard nie zostal znaleziony! Restrykcje regionow w CleanerController nie beda dzialac.");
        }

        startCleanerTask();
    }

    private void startCleanerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                Iterator<Map.Entry<Location, Long>> iterator = temporaryBlocks.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<Location, Long> entry = iterator.next();
                    if (currentTime >= entry.getValue()) {
                        Block block = entry.getKey().getBlock();
                        if (block.getType() != Material.AIR) {
                            block.setType(Material.AIR);
                        }
                        iterator.remove();
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Sprawdza co sekunde
    }

    public void clearAllStoredBlocks() {
        if (temporaryBlocks.isEmpty()) return;
        for (Location loc : temporaryBlocks.keySet()) {
            Block block = loc.getBlock();
            if (block.getType() != Material.AIR) {
                block.setType(Material.AIR);
            }
        }
        temporaryBlocks.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handlePlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        String worldName = block.getWorld().getName();
        Material type = block.getType();

        String endWorldName = config.getConfig().getString("settings.end_world_name", "spawn_the_end");
        String endRegionName = config.getConfig().getString("settings.end_spawn_region", "spawnend");

        if (worldName.equals(endWorldName)) {
            if (isInsideRegion(loc, endRegionName)) {
                event.setCancelled(true);
                return;
            }

            List<String> allowedEndPlace = config.getConfig().getStringList("settings.allowed_end_place_blocks");
            if (!allowedEndPlace.contains(type.name())) {
                event.setCancelled(true);
                return;
            }
        }

        List<String> enabledWorlds = config.getConfig().getStringList("settings.enabled_worlds");
        List<String> temporaryMaterialNames = config.getConfig().getStringList("settings.temporary_blocks");

        if (!event.isCancelled() && enabledWorlds.contains(worldName) && temporaryMaterialNames.contains(type.name())) {
            long expireTime = System.currentTimeMillis() + (config.getConfig().getInt("settings.live_time_seconds", 60) * 1000L);
            temporaryBlocks.put(loc, expireTime);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        String worldName = block.getWorld().getName();
        Material type = block.getType();

        String endWorldName = config.getConfig().getString("settings.end_world_name", "spawn_the_end");
        String endRegionName = config.getConfig().getString("settings.end_spawn_region", "spawnend");

        if (worldName.equals(endWorldName)) {
            if (isInsideRegion(loc, endRegionName)) {
                event.setCancelled(true);
                return;
            }

            List<String> allowedEndBreak = config.getConfig().getStringList("settings.allowed_end_break_blocks");
            if (!allowedEndBreak.contains(type.name())) {
                event.setCancelled(true);
                return;
            }
        }

        if (!event.isCancelled()) {
            temporaryBlocks.remove(loc);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleCrystalPlace(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == Material.END_CRYSTAL) {
            if (event.getClickedBlock() == null) return;

            Location loc = event.getClickedBlock().getLocation();
            String endWorldName = config.getConfig().getString("settings.end_world_name", "spawn_the_end");
            String endRegionName = config.getConfig().getString("settings.end_spawn_region", "spawnend");

            if (loc.getWorld().getName().equals(endWorldName)) {
                if (isInsideRegion(loc, endRegionName)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isInsideRegion(Location loc, String regionName) {
        if (regionQuery == null) return false;
        try {
            ApplicableRegionSet set = regionQuery.getApplicableRegions(BukkitAdapter.adapt(loc));
            for (ProtectedRegion region : set) {
                if (region.getId().equalsIgnoreCase(regionName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}