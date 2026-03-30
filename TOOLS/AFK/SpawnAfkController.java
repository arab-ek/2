package dev.arab.TOOLS.AFK;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnAfkController implements Listener {
    private final Main plugin;
    private final ConfigAfk config;
    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> idleSeconds = new ConcurrentHashMap<>();
    private RegionQuery regionQuery;

    public SpawnAfkController(Main plugin, ConfigAfk config) {
        this.plugin = plugin;
        this.config = config;

        try {
            this.regionQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        } catch (NoClassDefFoundError ignored) {}

        startIdleTask();
    }

    private void startIdleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!config.getConfig().getBoolean("settings.spawn_afk.enabled")) return;
                if (regionQuery == null) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    Location currentLoc = player.getLocation();
                    Location lastLoc = lastLocations.get(uuid);

                    if (lastLoc != null && currentLoc.getWorld().equals(lastLoc.getWorld()) &&
                            currentLoc.getBlockX() == lastLoc.getBlockX() &&
                            currentLoc.getBlockY() == lastLoc.getBlockY() &&
                            currentLoc.getBlockZ() == lastLoc.getBlockZ()) {

                        int currentIdle = idleSeconds.getOrDefault(uuid, 0) + 1;
                        idleSeconds.put(uuid, currentIdle);

                        if (currentIdle >= config.getConfig().getInt("settings.spawn_afk.max_idle_seconds")) {
                            if (isInSpawnRegion(player)) {
                                teleportToAfk(player);
                            }
                            idleSeconds.put(uuid, 0);
                        }
                    } else {
                        idleSeconds.put(uuid, 0);
                    }
                    lastLocations.put(uuid, currentLoc.clone());
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private boolean isInSpawnRegion(Player player) {
        try {
            ApplicableRegionSet set = regionQuery.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
            for (ProtectedRegion region : set) {
                if (region.getId().equalsIgnoreCase(config.getConfig().getString("settings.spawn_afk.region_name"))) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void teleportToAfk(Player player) {
        World afkWorld = Bukkit.getWorld(config.getConfig().getString("settings.spawn_afk.teleport_world"));
        if (afkWorld != null) {
            player.teleportAsync(afkWorld.getSpawnLocation()).thenRun(() -> {
                player.sendMessage(config.getMessage("spawn_afk_teleported"));
            });
        } else {
            plugin.getLogger().warning("Nie mozna przeteleportowac gracza " + player.getName() + " do AFK - swiat z configu nie istnieje!");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastLocations.remove(uuid);
        idleSeconds.remove(uuid);
    }
}