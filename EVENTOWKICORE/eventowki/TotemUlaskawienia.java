package dev.arab.EVENTOWKICORE.eventowki;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

public class TotemUlaskawienia extends EventItem implements Listener {
  private final Set<UUID> pendingResurrections = ConcurrentHashMap.newKeySet();
  private final Set<UUID> usedTotemPlayers = ConcurrentHashMap.newKeySet();
  private final Set<UUID> pendingRemoval = ConcurrentHashMap.newKeySet();
  private boolean isInfinite;
  private int customCooldown;
  private String useTitle;
  private String useSubtitle;

  public TotemUlaskawienia(Main plugin) {
    super(plugin, "totem_ulaskawienia");
  }

  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.isInfinite = this.plugin.getConfig().getBoolean("meta.totem_ulaskawienia.infinite", false);
    this.customCooldown = this.plugin.getConfig().getInt("meta.totem_ulaskawienia.cooldown", 15);
    this.useTitle = this.plugin.getMessagesConfig().getString("items.totem_ulaskawienia.use_title");
    this.useSubtitle = this.plugin.getMessagesConfig().getString("items.totem_ulaskawienia.use_subtitle");
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    ItemStack totem = this.getTotem(player);
    if (totem != null && !this.checkCooldown(player, totem)) {
      event.setKeepInventory(true);
      event.setKeepLevel(true);
      event.setDroppedExp(0);
      event.getDrops().clear();

      if (!this.isInfinite && !this.plugin.getTrybTworcyManager().hasModeEnabled(player)) {
        this.pendingRemoval.add(player.getUniqueId());
        this.clearTotems(player);
      }

      player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);
      player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation(), 50, 0.5D, 1.0D, 0.5D, 0.5D);
      if (this.customCooldown > 0) {
        this.plugin.getCooldownManager().setCooldown(player.getUniqueId(), this.id, this.customCooldown);
      }

      this.pendingResurrections.add(player.getUniqueId());
      this.usedTotemPlayers.add(player.getUniqueId());
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
        this.usedTotemPlayers.remove(player.getUniqueId());
      }, 1L);
    }
  }

  private ItemStack getTotem(Player player) {
    // Zwraca totem TYLKO wtedy, gdy gracz ma go w głównej ręce lub w drugiej ręce
    if (this.isSameItem(player.getInventory().getItemInMainHand())) {
      return player.getInventory().getItemInMainHand();
    } else if (this.isSameItem(player.getInventory().getItemInOffHand())) {
      return player.getInventory().getItemInOffHand();
    }

    // Zwracamy null, jeśli totem jest gdzieś głęboko w ekwipunku
    return null;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onResurrect(EntityResurrectEvent event) {
    Entity entity = event.getEntity();
    if (entity instanceof Player player) {
      if (this.isSameItem(player.getInventory().getItemInMainHand()) || this.isSameItem(player.getInventory().getItemInOffHand())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();

    if (this.pendingRemoval.contains(uuid)) {
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
        if (this.pendingRemoval.remove(uuid)) {
          this.clearTotems(player);
        }
      }, 1L);
    }

    if (this.pendingResurrections.remove(uuid) && (this.useTitle != null || this.useSubtitle != null)) {
      player.sendTitle(ChatUtils.color(this.useTitle != null ? this.useTitle : ""), ChatUtils.color(this.useSubtitle != null ? this.useSubtitle : ""), 5, 40, 5);
    }
  }

  private void clearTotems(Player player) {
    PlayerInventory inv = player.getInventory();
    boolean changed = false;

    // Usuwamy (zużywamy 1 sztukę) z głównej ręki...
    ItemStack mainHand = inv.getItemInMainHand();
    if (this.isSameItem(mainHand)) {
      mainHand.setAmount(mainHand.getAmount() - 1);
      inv.setItemInMainHand(mainHand.getAmount() > 0 ? mainHand : null);
      changed = true;
    }
    // ...albo z drugiej, jeśli w głównej nie było
    else {
      ItemStack offHand = inv.getItemInOffHand();
      if (this.isSameItem(offHand)) {
        offHand.setAmount(offHand.getAmount() - 1);
        inv.setItemInOffHand(offHand.getAmount() > 0 ? offHand : null);
        changed = true;
      }
    }

    if (changed) {
      player.updateInventory();
    }
  }

  private boolean isSameItem(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      String cid = item.getItemMeta().getPersistentDataContainer().get(this.itemKey, PersistentDataType.STRING);
      return this.id.equals(cid);
    }
    return false;
  }

  public boolean hasUsedTotem(UUID playerUUID) {
    return this.usedTotemPlayers.contains(playerUUID);
  }
}