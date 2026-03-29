/* Decompiler 339ms, total 863ms, lines 519 */
package dev.arab.EVENTOWKICORE.eventowki;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;

public class EventItemManager {
  private final Main plugin;
  private final Map<String, EventItem> items = new HashMap();
  private final Map<Material, List<EventItem>> materialMap = new HashMap();
  private final List<EventItem> moveListeners = new ArrayList();
  private final List<EventItem> jumpListeners = new ArrayList();
  private final List<EventItem> blockBreakListeners = new ArrayList();
  private final List<EventItem> respawnListeners = new ArrayList();
  private final List<EventItem> teleportListeners = new ArrayList();
  private final List<EventItem> toggleFlightListeners = new ArrayList();
  private final NamespacedKey itemKey;

  public EventItemManager(Main plugin) {
    this.plugin = plugin;
    this.itemKey = new NamespacedKey(plugin, "event_item_id");
  }

  public boolean isImmune(Entity entity) {
    if (entity instanceof Player) {
      Player player = (Player)entity;
      if (this.plugin.getCostumeManager() != null && this.plugin.getCostumeManager().isGrinchSkillActive(player)) {
        return true;
      }

      if (BoskiTopor.isImmortal(player.getUniqueId())) {
        return true;
      }
    }

    return false;
  }

  public void registerItem(EventItem item) {
    this.items.put(item.getId(), item);
    Material mat = item.getBaseMaterial();
    if (mat != null) {
      ((List)this.materialMap.computeIfAbsent(mat, (k) -> {
        return new ArrayList();
      })).add(item);
    }

    if (this.overridesMethod(item, "onMove", PlayerMoveEvent.class, Player.class)) {
      this.moveListeners.add(item);
    }

    if (this.overridesMethod(item, "onJump", PlayerJumpEvent.class, Player.class)) {
      this.jumpListeners.add(item);
    }

    if (this.overridesMethod(item, "onBlockBreak", BlockBreakEvent.class, Player.class)) {
      this.blockBreakListeners.add(item);
    }

    if (this.overridesMethod(item, "onRespawn", PlayerRespawnEvent.class, Player.class)) {
      this.respawnListeners.add(item);
    }

    if (this.overridesMethod(item, "onTeleport", PlayerTeleportEvent.class, Player.class)) {
      this.teleportListeners.add(item);
    }

    if (this.overridesMethod(item, "onToggleFlight", PlayerToggleFlightEvent.class, Player.class)) {
      this.toggleFlightListeners.add(item);
    }

    this.plugin.getServer().getPluginManager().registerEvents(item, this.plugin);
  }

  private boolean overridesMethod(EventItem item, String methodName, Class<?>... parameterTypes) {
    try {
      return item.getClass().getMethod(methodName, parameterTypes).getDeclaringClass() != EventItem.class;
    } catch (NoSuchMethodException var5) {
      return false;
    }
  }

  public EventItem getItem(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Material type = item.getType();
      if (!this.materialMap.containsKey(type)) {
        return null;
      } else {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
          return null;
        } else {
          PersistentDataContainer pdc = meta.getPersistentDataContainer();
          String id = (String)pdc.get(this.itemKey, PersistentDataType.STRING);
          if (id != null) {
            EventItem eventItem = (EventItem)this.items.get(id);
            if (eventItem != null) {
              if (eventItem.updateItem(item, meta)) {
                item.setItemMeta(meta);
              }

              return eventItem;
            } else {
              return null;
            }
          } else {
            if (meta.hasLore()) {
              List<EventItem> possible = (List)this.materialMap.get(type);
              if (possible != null) {
                Iterator var7 = possible.iterator();

                while(var7.hasNext()) {
                  EventItem eventItem = (EventItem)var7.next();
                  if (eventItem.isCustomItem(item, meta)) {
                    pdc.set(this.itemKey, PersistentDataType.STRING, eventItem.getId());
                    eventItem.updateItem(item, meta);
                    item.setItemMeta(meta);
                    return eventItem;
                  }
                }
              }
            }

            return null;
          }
        }
      }
    } else {
      return null;
    }
  }

  public EventItem getItemById(String id) {
    return (EventItem)this.items.get(id);
  }

  public boolean isEventItem(ItemStack item) {
    return this.getItem(item) != null;
  }

  public boolean hasId(ItemStack item) {
    return this.isEventItem(item);
  }

  public void dispatchMove(PlayerMoveEvent event) {
    if (!this.isImmune(event.getPlayer())) {
      Iterator var2 = this.moveListeners.iterator();

      while(var2.hasNext()) {
        EventItem item = (EventItem)var2.next();
        item.onMove(event, event.getPlayer());
      }

    }
  }

  public void dispatchInteract(PlayerInteractEvent event) {
    ItemStack item = event.getItem();
    if (item != null && item.getType() != Material.AIR) {
      EventItem eventItem = this.getItem(item);
      if (eventItem != null) {
        if (eventItem.isBlockedByCage() && !this.isImmune(event.getPlayer()) && HydroKlatka.isInsideOrRecentlyInsideCage(event.getPlayer())) {
          event.setCancelled(true);
          eventItem.sendBlockedNotification(event.getPlayer());
          return;
        }

        if (event.getAction().name().contains("BLOCK") && event.getClickedBlock() != null) {
          Location targetLoc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
          if (eventItem.isLocationBlocked(targetLoc)) {
            event.setCancelled(true);
            eventItem.sendBlockedNotification(event.getPlayer());
            return;
          }
        }

        eventItem.onInteract(event, event.getPlayer(), item);
      }

    }
  }

  public void dispatchDamage(EntityDamageByEntityEvent event) {
    Entity var3 = event.getEntity();
    if (var3 instanceof Player) {
      Player victim = (Player)var3;
      Player damager = event.getDamager() instanceof Player ? (Player)event.getDamager() : null;
      ItemStack mainHand = victim.getInventory().getItemInMainHand();
      EventItem vMain = this.getItem(mainHand);
      if (vMain != null) {
        vMain.onDamageVictim(event, victim, damager, mainHand);
      }

      ItemStack offHand = victim.getInventory().getItemInOffHand();
      EventItem vOff = this.getItem(offHand);
      if (vOff != null) {
        vOff.onDamageVictim(event, victim, damager, offHand);
      }

      if (damager != null) {
        ItemStack dMain = damager.getInventory().getItemInMainHand();
        EventItem dItem = this.getItem(dMain);
        if (dItem != null) {
          if (dItem.isLocationBlocked(victim.getLocation())) {
            event.setCancelled(true);
            dItem.sendTargetBlockedNotification(damager);
            return;
          }

          if (this.isImmune(victim)) {
            return;
          }

          dItem.onDamageDamager(event, damager, victim, dMain);
        }
      }

    }
  }

  public void dispatchFish(PlayerFishEvent event) {
    Player player = event.getPlayer();
    if (event.getCaught() != null && this.isImmune(event.getCaught())) {
      event.setCancelled(true);
    } else {
      ItemStack main = player.getInventory().getItemInMainHand();
      EventItem mainItem = this.getItem(main);
      if (mainItem != null && main.getType() == Material.FISHING_ROD) {
        mainItem.onFish(event, player, main);
      } else {
        ItemStack off = player.getInventory().getItemInOffHand();
        EventItem offItem = this.getItem(off);
        if (offItem != null && off.getType() == Material.FISHING_ROD) {
          offItem.onFish(event, player, off);
        }

      }
    }
  }

  public void dispatchConsume(PlayerItemConsumeEvent event) {
    EventItem eventItem = this.getItem(event.getItem());
    if (eventItem != null) {
      eventItem.onConsume(event, event.getPlayer(), event.getItem());
    }

  }

  public void dispatchLaunch(ProjectileLaunchEvent event) {
    ProjectileSource var3 = event.getEntity().getShooter();
    if (var3 instanceof Player) {
      Player player = (Player)var3;
      ItemStack main = player.getInventory().getItemInMainHand();
      EventItem mainItem = this.getItem(main);
      if (mainItem != null) {
        mainItem.onLaunch(event, player, main);
      } else {
        ItemStack off = player.getInventory().getItemInOffHand();
        EventItem offItem = this.getItem(off);
        if (offItem != null) {
          offItem.onLaunch(event, player, off);
        }

      }
    }
  }

  public void dispatchShootBow(EntityShootBowEvent event) {
    LivingEntity var3 = event.getEntity();
    if (var3 instanceof Player) {
      Player player = (Player)var3;
      EventItem item = this.getItem(event.getBow());
      if (item != null) {
        item.onShootBow(event, player, event.getBow());
      }
    }

  }

  public void dispatchProjectileHit(ProjectileHitEvent event) {
    String id = null;
    if (event.getEntity().getPersistentDataContainer().has(this.itemKey, PersistentDataType.STRING)) {
      id = (String)event.getEntity().getPersistentDataContainer().get(this.itemKey, PersistentDataType.STRING);
    } else if (event.getEntity().hasMetadata("custom_item")) {
      id = ((MetadataValue)event.getEntity().getMetadata("custom_item").get(0)).asString();
    }

    if (id != null) {
      EventItem item = (EventItem)this.items.get(id);
      if (item != null) {
        Player shooter = event.getEntity().getShooter() instanceof Player ? (Player)event.getEntity().getShooter() : null;
        if (item.isLocationBlocked(event.getEntity().getLocation()) || event.getHitBlock() != null && item.isLocationBlocked(event.getHitBlock().getLocation())) {
          event.setCancelled(true);
          event.getEntity().remove();
          return;
        }

        if (event.getHitEntity() != null && this.isImmune(event.getHitEntity())) {
          event.setCancelled(true);
          event.getEntity().remove();
          return;
        }

        item.onProjectileHit(event, shooter);
      }
    }

  }

  public void dispatchElytraToggle(EntityToggleGlideEvent event) {
    Entity var3 = event.getEntity();
    if (var3 instanceof Player) {
      Player player = (Player)var3;
      ItemStack chest = player.getInventory().getChestplate();
      EventItem item = this.getItem(chest);
      if (item != null) {
        item.onElytraToggle(event, player, chest);
      }
    }

  }

  public void dispatchEntityExplode(EntityExplodeEvent event) {
    if (event.getEntity().getPersistentDataContainer().has(this.itemKey, PersistentDataType.STRING)) {
      String id = (String)event.getEntity().getPersistentDataContainer().get(this.itemKey, PersistentDataType.STRING);
      EventItem item = (EventItem)this.items.get(id);
      if (item != null) {
        item.onEntityExplode(event);
      }
    }

  }

  public void dispatchEntityDeath(EntityDeathEvent event) {
    LivingEntity var3 = event.getEntity();
    if (var3 instanceof Player) {
      Player victim = (Player)var3;
      Player killer = victim.getKiller();
      if (killer != null) {
        if (!this.plugin.getTrybTworcyManager().hasModeEnabled(killer)) {
          if (!this.isImmune(victim)) {
            PlayerInventory inv = killer.getInventory();
            ItemStack[] contents = inv.getStorageContents();

            for(int i = 0; i < contents.length; ++i) {
              ItemStack item = contents[i];
              if (item != null && item.getType() != Material.AIR) {
                EventItem eventItem = this.getItem(item);
                if (eventItem != null) {
                  eventItem.onEntityKill(event, killer, victim, item);
                  inv.setItem(i, item);
                }
              }
            }

            ItemStack offHand = inv.getItemInOffHand();
            if (offHand != null && offHand.getType() != Material.AIR) {
              EventItem eventItem = this.getItem(offHand);
              if (eventItem != null) {
                eventItem.onEntityKill(event, killer, victim, offHand);
                inv.setItemInOffHand(offHand);
              }
            }

          }
        }
      }
    }
  }

  public void dispatchDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    ItemStack mainHand = player.getInventory().getItemInMainHand();
    EventItem itemMain = this.getItem(mainHand);
    if (itemMain != null) {
      itemMain.onDeath(event, player, mainHand);
    }

    ItemStack offHand = player.getInventory().getItemInOffHand();
    EventItem itemOff = this.getItem(offHand);
    if (itemOff != null) {
      itemOff.onDeath(event, player, offHand);
    }

  }

  public void dispatchResurrect(EntityResurrectEvent event) {
    LivingEntity var3 = event.getEntity();
    if (var3 instanceof Player) {
      Player player = (Player)var3;
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      EventItem itemMain = this.getItem(mainHand);
      if (itemMain != null) {
        itemMain.onResurrect(event, player, mainHand);
      }

      ItemStack offHand = player.getInventory().getItemInOffHand();
      EventItem itemOff = this.getItem(offHand);
      if (itemOff != null) {
        itemOff.onResurrect(event, player, offHand);
      }
    }

  }

  public void dispatchRespawn(PlayerRespawnEvent event) {
    Iterator var2 = this.respawnListeners.iterator();

    while(var2.hasNext()) {
      EventItem item = (EventItem)var2.next();
      item.onRespawn(event, event.getPlayer());
    }

  }

  public void dispatchTeleport(PlayerTeleportEvent event) {
    Iterator var2 = this.teleportListeners.iterator();

    while(var2.hasNext()) {
      EventItem item = (EventItem)var2.next();
      item.onTeleport(event, event.getPlayer());
    }

  }

  public void dispatchToggleFlight(PlayerToggleFlightEvent event) {
    Iterator var2 = this.toggleFlightListeners.iterator();

    while(var2.hasNext()) {
      EventItem item = (EventItem)var2.next();
      item.onToggleFlight(event, event.getPlayer());
    }

  }

  public void dispatchBlockPlace(BlockPlaceEvent event) {
    EventItem item = this.getItem(event.getItemInHand());
    if (item != null) {
      item.onBlockPlace(event, event.getPlayer(), event.getItemInHand());
    }

  }

  public void dispatchBlockBreak(BlockBreakEvent event) {
    if (!this.isImmune(event.getPlayer())) {
      Iterator var2 = this.blockBreakListeners.iterator();

      while(var2.hasNext()) {
        EventItem item = (EventItem)var2.next();
        item.onBlockBreak(event, event.getPlayer());
      }

    }
  }

  public void dispatchJump(PlayerJumpEvent event) {
    Iterator var2 = this.jumpListeners.iterator();

    while(var2.hasNext()) {
      EventItem item = (EventItem)var2.next();
      item.onJump(event, event.getPlayer());
    }

  }

  public void updateAllInventories() {
    Iterator var1 = this.plugin.getServer().getOnlinePlayers().iterator();

    while(var1.hasNext()) {
      Player player = (Player)var1.next();
      PlayerInventory inv = player.getInventory();
      int size = inv.getSize();

      for(int i = 0; i < size; ++i) {
        ItemStack item = inv.getItem(i);
        if (item != null && item.getType() != Material.AIR && this.materialMap.containsKey(item.getType())) {
          this.getItem(item);
        }
      }
    }

  }
}