/* Decompiler 119ms, total 386ms, lines 231 */
package dev.arab.EVENTOWKICORE.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.eventowki.EventItem;
import dev.arab.KSIEGI.ksiazki.Book;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class EquipmentCacheManager implements Listener {
  private final Main plugin;
  private final Map<UUID, EquipmentCacheManager.PlayerEquipment> cache = new ConcurrentHashMap();
  private final Map<UUID, Long> lastInteract = new ConcurrentHashMap();
  private final NamespacedKey chargeKey;

  public EquipmentCacheManager(Main plugin) {
    this.plugin = plugin;
    this.chargeKey = new NamespacedKey(plugin, "elytra_charge");
  }

  public void setElytraCharge(UUID uuid, double charge) {
    EquipmentCacheManager.PlayerEquipment eq = (EquipmentCacheManager.PlayerEquipment)this.cache.get(uuid);
    if (eq != null) {
      eq.elytraCharge = charge;
    }

  }

  public EquipmentCacheManager.PlayerEquipment getEquipment(UUID uuid) {
    return (EquipmentCacheManager.PlayerEquipment)this.cache.get(uuid);
  }

  public void updateCache(Player player) {
    EquipmentCacheManager.PlayerEquipment equipment = new EquipmentCacheManager.PlayerEquipment();
    int heldSlot = player.getInventory().getHeldItemSlot();
    this.scanSlot(equipment, player, 40);
    this.scanSlot(equipment, player, 39);
    this.scanSlot(equipment, player, 38);
    this.scanSlot(equipment, player, 37);
    this.scanSlot(equipment, player, 36);
    this.scanSlot(equipment, player, heldSlot);
    this.cache.put(player.getUniqueId(), equipment);
  }

  private void updateSingleSlot(Player player, int slot) {
    EquipmentCacheManager.PlayerEquipment eq = (EquipmentCacheManager.PlayerEquipment)this.cache.computeIfAbsent(player.getUniqueId(), (k) -> {
      return new EquipmentCacheManager.PlayerEquipment();
    });
    eq.slotBooks.remove(slot);
    eq.slotEventItems.remove(slot);
    if (eq.elytraSlot != null && eq.elytraSlot == slot) {
      eq.hasWzmocnionaElytra = false;
      eq.elytraCharge = null;
      eq.elytraSlot = null;
    }

    this.scanSlot(eq, player, slot);
  }

  private void scanSlot(EquipmentCacheManager.PlayerEquipment equipment, Player player, int slot) {
    ItemStack item = player.getInventory().getItem(slot);
    if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
        if (this.plugin.getBookManager().hasCustomBook(item, meta)) {
          List<Book> books = null;
          Iterator var7 = this.plugin.getBookManager().getBooks().iterator();

          while(var7.hasNext()) {
            Book book = (Book)var7.next();
            if (book.hasBook(meta) && book.canApply(item)) {
              if (books == null) {
                books = new ArrayList();
              }

              books.add(book);
            }
          }

          if (books != null) {
            equipment.slotBooks.put(slot, books);
          }
        }

        EventItem eventItem = this.plugin.getEventItemManager().getItem(item);
        if (eventItem != null) {
          equipment.slotEventItems.put(slot, eventItem);
          if (slot == 38 && "wzmocniona_elytra".equals(eventItem.getId())) {
            equipment.hasWzmocnionaElytra = true;
            equipment.elytraCharge = (Double)meta.getPersistentDataContainer().getOrDefault(this.chargeKey, PersistentDataType.DOUBLE, 0.0D);
            equipment.elytraSlot = slot;
          }
        }

      }
    }
  }

  @EventHandler(
          priority = EventPriority.MONITOR,
          ignoreCancelled = true
  )
  public void onJoin(PlayerJoinEvent event) {
    this.updateCache(event.getPlayer());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    this.cache.remove(uuid);
    this.lastInteract.remove(uuid);
  }

  @EventHandler(
          priority = EventPriority.MONITOR,
          ignoreCancelled = true
  )
  public void onInventoryClick(InventoryClickEvent event) {
    HumanEntity var3 = event.getWhoClicked();
    if (var3 instanceof Player) {
      Player player = (Player)var3;
      int slot = event.getSlot();
      if ((slot < 36 || slot > 40) && slot != player.getInventory().getHeldItemSlot()) {
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
          this.updateCache(player);
        });
      } else {
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
          this.updateSingleSlot(player, slot);
        });
      }
    }

  }

  @EventHandler(
          priority = EventPriority.MONITOR,
          ignoreCancelled = true
  )
  public void onInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    long now = System.currentTimeMillis();
    Long last = (Long)this.lastInteract.get(player.getUniqueId());
    if (last == null || now - last >= 500L) {
      this.lastInteract.put(player.getUniqueId(), now);
      this.updateSingleSlot(player, player.getInventory().getHeldItemSlot());
    }
  }

  @EventHandler(
          priority = EventPriority.MONITOR,
          ignoreCancelled = true
  )
  public void onSwap(PlayerSwapHandItemsEvent event) {
    this.updateCache(event.getPlayer());
  }

  @EventHandler(
          priority = EventPriority.MONITOR,
          ignoreCancelled = true
  )
  public void onHeld(PlayerItemHeldEvent event) {
    Player player = event.getPlayer();
    this.updateSingleSlot(player, event.getNewSlot());
    this.updateSingleSlot(player, event.getPreviousSlot());
  }

  @EventHandler(
          priority = EventPriority.MONITOR,
          ignoreCancelled = true
  )
  public void onPickup(EntityPickupItemEvent event) {
    LivingEntity var3 = event.getEntity();
    if (var3 instanceof Player) {
      Player player = (Player)var3;
      this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
        this.updateCache(player);
      });
    }

  }

  @EventHandler(
          priority = EventPriority.MONITOR,
          ignoreCancelled = true
  )
  public void onDrop(PlayerDropItemEvent event) {
    this.updateSingleSlot(event.getPlayer(), event.getPlayer().getInventory().getHeldItemSlot());
  }

  @EventHandler(
          priority = EventPriority.MONITOR,
          ignoreCancelled = true
  )
  public void onBreak(PlayerItemBreakEvent event) {
    this.updateCache(event.getPlayer());
  }

  public static class PlayerEquipment {
    public final Map<Integer, List<Book>> slotBooks = new HashMap();
    public final Map<Integer, EventItem> slotEventItems = new HashMap();
    public boolean hasWzmocnionaElytra = false;
    public Double elytraCharge = null;
    public Integer elytraSlot = null;
  }
}