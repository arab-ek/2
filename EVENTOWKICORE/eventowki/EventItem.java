package dev.arab.EVENTOWKICORE.eventowki;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import java.util.ArrayList;
import java.util.List;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public abstract class EventItem implements Listener {
  protected final Main plugin;
  protected final String id;
  protected final NamespacedKey itemKey;
  protected final NamespacedKey uidKey;
  private Material baseMaterial;
  private String cachedDisplayName;
  private List<String> cachedLore;
  private List<String> globalBlockedWorlds;
  private List<String> itemBlockedWorlds;
  private List<String> itemBlockedRegions;
  private boolean vanillaCooldown;
  protected boolean infinite;
  private int defaultCooldown;

  public EventItem(Main plugin, String id) {
    this.plugin = plugin;
    this.id = id;
    this.itemKey = new NamespacedKey(plugin, "event_item_id");
    this.uidKey = new NamespacedKey(plugin, "event_item_uid");
    this.reloadConfigCache();
  }

    protected EventItem() {
    }

    public void reloadConfigCache() {
    String materialName = this.plugin.getConfig().getString("meta." + this.id + ".material");
    if (materialName != null && !materialName.isEmpty()) {
      try {
        this.baseMaterial = Material.valueOf(materialName.toUpperCase());
      } catch (IllegalArgumentException e) {
        this.baseMaterial = null;
      }
    }

    String name = this.plugin.getConfig().getString("meta." + this.id + ".name");
    this.cachedDisplayName = name != null ? ChatUtils.color(name) : null;
    List<String> lore = this.plugin.getConfig().getStringList("meta." + this.id + ".lore");
    if (lore != null && !lore.isEmpty()) {
      this.cachedLore = new ArrayList<>();
      for (String line : lore) {
        this.cachedLore.add(ChatUtils.color(line));
      }
    } else {
      this.cachedLore = null;
    }

    this.globalBlockedWorlds = this.plugin.getConfig().getStringList("blocked_worlds");
    this.itemBlockedWorlds = this.plugin.getConfig().getStringList("meta." + this.id + ".blocked_worlds");
    this.itemBlockedRegions = this.plugin.getConfig().getStringList("meta." + this.id + ".blocked_regions");
    this.vanillaCooldown = this.plugin.getConfig().getBoolean("meta." + this.id + ".vanilla_cooldown", true);
    this.infinite = this.plugin.getConfig().getBoolean("meta." + this.id + ".infinite", true);
    this.defaultCooldown = this.plugin.getConfig().getInt("meta." + this.id + ".cooldown", 15);
  }

  public Material getBaseMaterial() {
    return this.baseMaterial;
  }

  public String getId() {
    return this.id;
  }

  public void onGive(Player player, ItemStack item) {}
  public void onMove(PlayerMoveEvent event, Player player) {}
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {}
  public void onJump(PlayerJumpEvent event, Player player) {}
  public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {}
  public void onDamageVictim(EntityDamageByEntityEvent event, Player victim, Player damager, ItemStack item) {}
  public void onFish(PlayerFishEvent event, Player player, ItemStack item) {}
  public void onConsume(PlayerItemConsumeEvent event, Player player, ItemStack item) {}
  public void onLaunch(ProjectileLaunchEvent event, Player player, ItemStack item) {}
  public void onShootBow(EntityShootBowEvent event, Player player, ItemStack item) {}
  public void onProjectileHit(ProjectileHitEvent event, Player shooter) {}
  public void onTeleport(PlayerTeleportEvent event, Player player) {}
  public void onElytraToggle(EntityToggleGlideEvent event, Player player, ItemStack item) {}
  public void onEntityExplode(EntityExplodeEvent event) {}
  public void onDeath(PlayerDeathEvent event, Player player, ItemStack item) {}
  public void onEntityKill(EntityDeathEvent event, Player killer, Player victim, ItemStack item) {}
  public void onResurrect(EntityResurrectEvent event, Player player, ItemStack item) {}
  public void onRespawn(PlayerRespawnEvent event, Player player) {}
  public void onBlockPlace(BlockPlaceEvent event, Player player, ItemStack item) {}
  public void onBlockBreak(BlockBreakEvent event, Player player) {}
  public void onToggleFlight(PlayerToggleFlightEvent event, Player player) {}

  public boolean isBlockedByCage() {
    return false;
  }

  protected boolean isBlocked(Player player) {
    return this.isBlocked(player, player.getLocation());
  }

  protected boolean isBlocked(Player player, Location checkLoc) {
    if (!this.globalBlockedWorlds.contains(checkLoc.getWorld().getName()) && !this.itemBlockedWorlds.contains(checkLoc.getWorld().getName())) {
      if (this.plugin.getItemListener().getWorldGuardHook().isLocationInBlockedRegion(checkLoc, this.itemBlockedRegions)) {
        this.sendBlockedNotification(player);
        return true;
      } else {
        return false;
      }
    } else {
      this.sendBlockedNotification(player);
      return true;
    }
  }

  public void sendBlockedNotification(Player player) {
    String title = this.plugin.getMessagesConfig().getString("messages.blocked_region_title", "&#FF0000✘");
    String subtitle = this.plugin.getMessagesConfig().getString("messages.blocked_region_subtitle", "&#EC0000Nie możesz tego użyć!");
    player.sendTitle(ChatUtils.color(title), ChatUtils.color(subtitle), 5, 20, 5);
  }

  public void sendTargetBlockedNotification(Player player) {
    String title = this.plugin.getMessagesConfig().getString("messages.blocked_region_title", "&#FF0000✘");
    String subtitle = this.plugin.getMessagesConfig().getString("messages.blocked_region_target_subtitle", "&#EC0000Gracz jest na bezpiecznym regionie!");
    player.sendTitle(ChatUtils.color(title), ChatUtils.color(subtitle), 5, 20, 5);
  }

  public boolean isLocationBlocked(Location loc) {
    return !this.globalBlockedWorlds.contains(loc.getWorld().getName()) && !this.itemBlockedWorlds.contains(loc.getWorld().getName()) ? this.plugin.getItemListener().getWorldGuardHook().isLocationInBlockedRegion(loc, this.itemBlockedRegions) : true;
  }

  protected boolean checkCooldown(Player player, ItemStack item) {
    return this.checkCooldown(player, item, this.id);
  }

  protected boolean checkCooldown(Player player, ItemStack item, String cooldownId) {
    return this.plugin.getCooldownManager().hasCooldown(player.getUniqueId(), this.getCooldownId(item, cooldownId));
  }

  protected String getCooldownId(ItemStack item, String baseCooldownId) {
    if (item != null && item.hasItemMeta()) {
      String uid = item.getItemMeta().getPersistentDataContainer().get(this.uidKey, PersistentDataType.STRING);
      if (uid != null) {
        return baseCooldownId + ":" + uid;
      }
    }
    return baseCooldownId;
  }

  protected void applyUse(Player player, Player victim, ItemStack item, String cooldownId) {
    this.applyUse(player, victim, item, cooldownId, this.id, true);
  }

  protected void applyUseSilent(Player player, Player victim, ItemStack item, String cooldownId) {
    this.applyUse(player, victim, item, cooldownId, this.id, false);
  }

  protected void applyUse(Player player, Player victim, ItemStack item, String cooldownId, String messagePath) {
    this.applyUse(player, victim, item, cooldownId, messagePath, true);
  }

  private void applyUse(Player player, Player victim, ItemStack item, String cooldownId, String messagePath, boolean sendTitle) {
    if (player != null && item != null) {
      String finalCooldownId = this.getCooldownId(item, cooldownId);
      String subKey = cooldownId.replace(this.id + ".", "");
      int cooldownSeconds = this.plugin.getConfig().getInt("meta." + this.id + "." + subKey + "_cooldown", this.defaultCooldown);
      if (cooldownSeconds > 0) {
        this.plugin.getCooldownManager().setCooldown(player.getUniqueId(), finalCooldownId, cooldownSeconds);
        if (this.vanillaCooldown) {
          player.setCooldown(item.getType(), cooldownSeconds * 20);
        }
      }

      if (sendTitle && this.plugin.getMessagesConfig().getBoolean("items." + messagePath + ".enabled", true)) {
        String title = this.plugin.getMessagesConfig().getString("items." + messagePath + ".use_title");
        String subtitle = this.plugin.getMessagesConfig().getString("items." + messagePath + ".use_subtitle");
        if (title != null || subtitle != null) {
          String itemName = this.plugin.getConfig().getString("meta." + this.id + ".name", this.id);
          player.sendTitle(ChatUtils.color(this.replacePlaceholders(title, player, victim, itemName)), ChatUtils.color(this.replacePlaceholders(subtitle, player, victim, itemName)), 5, 20, 5);
        }
      }

      if (!this.infinite) {
        item.setAmount(item.getAmount() - 1);
      }
    }
  }

  protected void sendUseNotification(Player player, Player victim) {
    if (this.plugin.getMessagesConfig().getBoolean("items." + this.id + ".enabled", true)) {
      String title = this.plugin.getMessagesConfig().getString("items." + this.id + ".use_title");
      String subtitle = this.plugin.getMessagesConfig().getString("items." + this.id + ".use_subtitle");
      if (title != null || subtitle != null) {
        String itemName = this.plugin.getConfig().getString("meta." + this.id + ".name", this.id);
        player.sendTitle(ChatUtils.color(this.replacePlaceholders(title, player, victim, itemName)), ChatUtils.color(this.replacePlaceholders(subtitle, player, victim, itemName)), 5, 20, 5);
      }
    }
  }

  protected void sendVictimNotification(Player victim, Player damager) {
    if (this.plugin.getMessagesConfig().getBoolean("items." + this.id + ".enabled", true)) {
      String title = this.plugin.getMessagesConfig().getString("items." + this.id + ".victim_title");
      String subtitle = this.plugin.getMessagesConfig().getString("items." + this.id + ".victim_subtitle");
      if (title != null || subtitle != null) {
        String itemName = this.plugin.getConfig().getString("meta." + this.id + ".name", this.id);
        victim.sendTitle(ChatUtils.color(this.replacePlaceholders(title, damager, victim, itemName)), ChatUtils.color(this.replacePlaceholders(subtitle, damager, victim, itemName)), 5, 20, 5);
      }
    }
  }

  protected String replacePlaceholders(String text, Player player, Player victim, String itemName) {
    if (text == null) {
      return null;
    } else {
      String pName = player != null ? player.getName() : "";
      String vName = victim != null ? victim.getName() : "";
      String iName = itemName != null ? itemName : "";
      return text.replace("%name%", iName).replace("%player%", pName).replace("%damager_player%", pName).replace("%victim_player%", vName).replace("%victim%", vName);
    }
  }

  public boolean isCustomItem(ItemStack item) {
    if (item != null && !item.getType().isAir() && item.hasItemMeta()) {
      ItemMeta meta = item.getItemMeta();
      return this.isCustomItem(item, meta);
    }
    return false;
  }

  public boolean isCustomItem(ItemStack item, ItemMeta meta) {
    if (meta != null && item.getType() == this.baseMaterial) {
      String cid = meta.getPersistentDataContainer().get(this.itemKey, PersistentDataType.STRING);
      if (cid != null) {
        return this.id.equals(cid);
      } else if (this.cachedDisplayName != null && meta.hasDisplayName() && meta.getDisplayName().equals(this.cachedDisplayName)) {
        return true;
      } else {
        if (this.cachedLore != null && meta.hasLore()) {
          List<String> itemLore = meta.getLore();
          if (itemLore.size() >= this.cachedLore.size()) {
            for (String line : itemLore) {
              if (this.cachedLore.contains(line)) {
                return true;
              }
            }
          }
        }
        return false;
      }
    }
    return false;
  }

  public boolean updateItem(ItemStack item, ItemMeta meta) {
    if (meta == null) {
      return false;
    }
    boolean changed = false;

    if (this.applyEnchants(meta)) {
      changed = true;
    }

    // Usunięcie unikalnego ID (UID), aby umożliwić swobodne stackowanie eventówek
    if (meta.getPersistentDataContainer().has(this.uidKey, PersistentDataType.STRING)) {
      meta.getPersistentDataContainer().remove(this.uidKey);
      changed = true;
    }

    int initialFlags = meta.getItemFlags().size();
    this.applyHideFlags(meta);
    if (meta.getItemFlags().size() > initialFlags) {
      changed = true;
    }

    return changed;
  }

  protected boolean applyEnchants(ItemMeta meta) {
    return false;
  }

  protected void applyHideFlags(ItemMeta meta) {
    if (meta != null && meta.getItemFlags().size() < ItemFlag.values().length) {
      meta.addItemFlags(ItemFlag.values());
    }
  }
}