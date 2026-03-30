package dev.arab.TOOLS.VOID;

import dev.arab.Main;
import dev.arab.EVENTOWKICORE.eventowki.EventItem;
import dev.arab.EVENTOWKICORE.utils.EquipmentCacheManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;

public class VoidDropController implements Listener {
    private final Main plugin;
    private final ConfigVoid config;

    public VoidDropController(Main plugin, ConfigVoid config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        String worldName = player.getWorld().getName();

        // Sprawdzamy czy system jest włączony na tym świecie
        if (!config.getConfig().getStringList("settings.enabled_worlds").contains(worldName)) return;

        EquipmentCacheManager cache = plugin.getEquipmentCacheManager();
        if (cache == null) return;

        EquipmentCacheManager.PlayerEquipment equipment = cache.getEquipment(player.getUniqueId());
        if (equipment == null) return;

        boolean hasRequiredItem = false;
        String requiredItemName = config.getConfig().getString("settings.required_event_item", "roza_kupidyna_2026");

        // Szukamy przedmiotu w cache szafy (event items)
        for (Map.Entry<Integer, EventItem> entry : equipment.slotEventItems.entrySet()) {
            if (entry.getValue().getId().equals(requiredItemName)) {
                hasRequiredItem = true;
                break;
            }
        }

        if (hasRequiredItem) {
            event.setCancelled(true); // Anulujemy dmg od voida
            teleportToSpawn(player);
        }
    }

    private void teleportToSpawn(Player player) {
        // Pobieramy spawn z configu void, aby nie uzależniać się od config-sprawdzania.yml
        String wName = config.getConfig().getString("locations.spawn.world", "world");
        World w = Bukkit.getWorld(wName);
        if (w == null) {
            plugin.getLogger().warning("Świat spawnu '" + wName + "' w config-void.yml nie istnieje!");
            return;
        }

        double x = config.getConfig().getDouble("locations.spawn.x");
        double y = config.getConfig().getDouble("locations.spawn.y");
        double z = config.getConfig().getDouble("locations.spawn.z");
        float yaw = (float) config.getConfig().getDouble("locations.spawn.yaw");
        float pitch = (float) config.getConfig().getDouble("locations.spawn.pitch");

        Location spawn = new Location(w, x, y, z, yaw, pitch);

        // Używamy Async Teleportacji
        player.teleportAsync(spawn).thenRun(() -> {
            player.sendMessage(config.getMessage("teleported"));
            player.setFallDistance(0.0F); // Zapobiega dmg od upadku zaraz po TP
        });
    }
}