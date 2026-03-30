package dev.arab.TOOLS.DRAGON;

import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class DragonBossListener implements Listener {
    private final DragonBossManager manager;

    public DragonBossListener(DragonBossManager manager) {
        this.manager = manager;
    }

    // Dodawanie paska BossBara nowym graczom, gdy smok żyje/trwa odliczanie
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (manager.getBossBar() != null) {
            manager.getBossBar().addPlayer(event.getPlayer());
        }
    }

    // Zliczanie obrażeń
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonDamage(EntityDamageByEntityEvent event) {
        if (manager.getActiveDragon() != null && event.getEntity().equals(manager.getActiveDragon())) {
            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                manager.addDamage(damager, event.getFinalDamage());
            }
        }
    }

    // Obsługa śmierci naszego smoka
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (manager.getActiveDragon() != null && event.getEntity().equals(manager.getActiveDragon())) {
            manager.handleDragonDeath();
        }
    }

    // --- ZABEZPIECZENIA PRZED NISZCZENIEM BLOKÓW ---

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDragonChangeBlock(EntityChangeBlockEvent event) {
        // Blokuje niszczenie bloków gdy smok przez nie przelatuje
        if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDragonExplode(EntityExplodeEvent event) {
        // Blokuje wybuchy generowane przez smoka (np. fireball)
        if (event.getEntityType() == EntityType.ENDER_DRAGON || event.getEntityType() == EntityType.DRAGON_FIREBALL) {
            event.blockList().clear();
        }
    }
}