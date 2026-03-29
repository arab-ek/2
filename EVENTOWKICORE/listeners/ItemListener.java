/* Decompiler 335ms, total 548ms, lines 269 */
package dev.arab.EVENTOWKICORE.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.eventowki.HydroKlatka;
import dev.arab.EVENTOWKICORE.hooks.WorldGuardHook;
import dev.arab.EVENTOWKICORE.utils.BlockTracker;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {
  private final Main plugin;
  private final WorldGuardHook worldGuardHook;
  private final BlockTracker blockTracker;
  private final Set<UUID> frozenPlayers = new HashSet();
  private final Map<UUID, FishHook> victimHooks = new HashMap();

  public ItemListener(Main plugin, WorldGuardHook worldGuardHook, BlockTracker blockTracker) {
    this.plugin = plugin;
    this.worldGuardHook = worldGuardHook;
    this.blockTracker = blockTracker;
    plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
      this.victimHooks.entrySet().removeIf((entry) -> {
        UUID uuid = (UUID)entry.getKey();
        FishHook hook = (FishHook)entry.getValue();
        Player p = plugin.getServer().getPlayer(uuid);
        if (p != null && hook != null && hook.isValid() && !hook.isDead()) {
          if (p.isGliding()) {
            p.setGliding(false);
          }

          return false;
        } else {
          return true;
        }
      });
    }, 5L, 5L);
  }

  public WorldGuardHook getWorldGuardHook() {
    return this.worldGuardHook;
  }

  public BlockTracker getBlockTracker() {
    return this.blockTracker;
  }

  public Map<UUID, FishHook> getVictimHooks() {
    return this.victimHooks;
  }

  public Set<UUID> getFrozenPlayers() {
    return this.frozenPlayers;
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    Location from = event.getFrom();
    Location to = event.getTo();
    if (to != null) {
      if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (this.frozenPlayers.contains(uuid)) {
          event.setTo(from.setDirection(to.getDirection()));
        }

        this.plugin.getEventItemManager().dispatchMove(event);
      }
    }
  }

  @EventHandler
  public void onTeleport(PlayerTeleportEvent event) {
    this.plugin.getEventItemManager().dispatchTeleport(event);
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    if (HydroKlatka.isInsideCage(event.getBlockPlaced().getLocation())) {
      event.setCancelled(true);
      String title = this.plugin.getMessagesConfig().getString("messages.blocked_region_title", "&#FF0000✘");
      String subtitle = this.plugin.getMessagesConfig().getString("messages.blocked_build_subtitle", "&#EC0000Nie możesz budować w klatce!");
      event.getPlayer().sendTitle(ChatUtils.color(title), ChatUtils.color(subtitle), 5, 20, 5);
    } else {
      if (event.getBlockPlaced().getType() == Material.BEDROCK) {
        this.blockTracker.addBlock(event.getBlockPlaced().getLocation());
      }

      this.plugin.getEventItemManager().dispatchBlockPlace(event);
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (event.getItem() != null && event.getItem().getType() == Material.ENDER_PEARL && HydroKlatka.isInsideOrRecentlyInsideCage(event.getPlayer())) {
      event.setCancelled(true);
      String title = this.plugin.getMessagesConfig().getString("messages.blocked_region_title", "&#FF0000✘");
      String subtitle = this.plugin.getMessagesConfig().getString("messages.blocked_pearl_subtitle", "&#EC0000Nie możesz używać pereł w klatce!");
      event.getPlayer().sendTitle(ChatUtils.color(title), ChatUtils.color(subtitle), 5, 20, 5);
    } else {
      this.plugin.getEventItemManager().dispatchInteract(event);
    }
  }

  @EventHandler
  public void onDamage(EntityDamageEvent event) {
    if (this.plugin.getEventItemManager().isImmune(event.getEntity())) {
      event.setCancelled(true);
    }

  }

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent event) {
    if (this.plugin.getEventItemManager().isImmune(event.getEntity())) {
      event.setCancelled(true);
    } else {
      this.plugin.getEventItemManager().dispatchDamage(event);
    }
  }

  @EventHandler
  public void onFish(PlayerFishEvent event) {
    this.plugin.getEventItemManager().dispatchFish(event);
  }

  @EventHandler
  public void onConsume(PlayerItemConsumeEvent event) {
    this.plugin.getEventItemManager().dispatchConsume(event);
  }

  @EventHandler
  public void onLaunch(ProjectileLaunchEvent event) {
    this.plugin.getEventItemManager().dispatchLaunch(event);
  }

  @EventHandler
  public void onShootBow(EntityShootBowEvent event) {
    this.plugin.getEventItemManager().dispatchShootBow(event);
  }

  @EventHandler
  public void onProjectileHit(ProjectileHitEvent event) {
    this.plugin.getEventItemManager().dispatchProjectileHit(event);
  }

  @EventHandler
  public void onElytraToggle(EntityToggleGlideEvent event) {
    if (event.isGliding()) {
      Entity var3 = event.getEntity();
      if (var3 instanceof Player) {
        Player player = (Player)var3;
        FishHook hook = (FishHook)this.victimHooks.get(player.getUniqueId());
        if (hook != null && hook.isValid()) {
          event.setCancelled(true);
        } else if (hook != null) {
          this.victimHooks.remove(player.getUniqueId());
        }
      }
    }

    this.plugin.getEventItemManager().dispatchElytraToggle(event);
  }

  @EventHandler
  public void onToggleFlight(PlayerToggleFlightEvent event) {
    this.plugin.getEventItemManager().dispatchToggleFlight(event);
  }

  @EventHandler
  public void onEntityExplode(EntityExplodeEvent event) {
    this.plugin.getEventItemManager().dispatchEntityExplode(event);
  }

  @EventHandler
  public void onKill(EntityDeathEvent event) {
    this.plugin.getEventItemManager().dispatchEntityDeath(event);
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    this.plugin.getEventItemManager().dispatchDeath(event);
  }

  @EventHandler
  public void onResurrect(EntityResurrectEvent event) {
    this.plugin.getEventItemManager().dispatchResurrect(event);
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent event) {
    this.plugin.getEventItemManager().dispatchRespawn(event);
  }

  @EventHandler(
          ignoreCancelled = true
  )
  public void onBlockBreak(BlockBreakEvent event) {
    this.plugin.getEventItemManager().dispatchBlockBreak(event);
  }

  @EventHandler
  public void onCreatureSpawn(CreatureSpawnEvent event) {
    if (event.getSpawnReason() == SpawnReason.EGG) {
      event.setCancelled(true);
    }

  }

  @EventHandler
  public void onJump(PlayerJumpEvent event) {
    this.plugin.getEventItemManager().dispatchJump(event);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!event.isCancelled()) {
      ItemStack cursor = event.getCursor();
      ItemStack current = event.getCurrentItem();
      if (cursor != null && cursor.getType() != Material.AIR) {
        if (this.plugin.getEventItemManager().isEventItem(cursor) && (event.getAction() == InventoryAction.PLACE_SOME || event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_ALL) && current != null && current.getType() != Material.AIR && this.plugin.getEventItemManager().isEventItem(current)) {
          event.setCancelled(true);
        }

        if (event.getClick().isShiftClick() && current != null && current.getType() != Material.AIR) {
          this.plugin.getEventItemManager().getItem(current);
        }

      }
    }
  }
}