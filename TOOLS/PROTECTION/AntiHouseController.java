package dev.arab.TOOLS.PROTECTION;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AntiHouseController implements Listener {
    private final ConfigProtection config;
    private final Map<UUID, List<Long>> placementTimes = new HashMap<>();

    public AntiHouseController(ConfigProtection config) {
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!config.getConfig().getBoolean("settings.anti_house.enabled", true)) return;

        Player player = event.getPlayer();
        if (player.hasPermission("core.admin")) return; // Adminów nie sprawdzamy

        Material type = event.getBlock().getType();
        List<String> monitoredBlocks = config.getConfig().getStringList("settings.anti_house.monitored_blocks");

        if (monitoredBlocks.contains(type.name())) {
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();

            placementTimes.putIfAbsent(uuid, new ArrayList<>());
            List<Long> times = placementTimes.get(uuid);

            long windowMs = config.getConfig().getLong("settings.anti_house.time_window_ms", 1250L);
            times.removeIf(time -> (now - time > windowMs));
            times.add(now);

            int threshold = config.getConfig().getInt("settings.anti_house.block_threshold", 10);
            if (times.size() >= threshold) {
                alertAdmins(player);
                times.clear();
            }
        }
    }

    private void alertAdmins(Player suspect) {
        String alertMessage = config.getMessage("anti_house_alert").replace("{PLAYER}", suspect.getName());

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("core.admin")) {
                admin.sendMessage(alertMessage);
            }
        }
    }
}