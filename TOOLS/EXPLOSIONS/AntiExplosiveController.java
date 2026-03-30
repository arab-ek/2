package dev.arab.TOOLS.EXPLOSIONS;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class AntiExplosiveController implements Listener {
    private final ConfigExplosions config;

    public AntiExplosiveController(ConfigExplosions config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();

        // Blokowanie niszczenia bloków przez TNT
        if (type == EntityType.PRIMED_TNT && config.getConfig().getBoolean("settings.block_tnt_block_damage")) {
            event.blockList().clear(); // Usuwa bloki z listy do zniszczenia, zachowując dmg dla encji
            return;
        }

        // Blokowanie niszczenia bloków przez Creepery
        if (type == EntityType.CREEPER && config.getConfig().getBoolean("settings.block_creeper_block_damage")) {
            event.blockList().clear();
        }
    }
}