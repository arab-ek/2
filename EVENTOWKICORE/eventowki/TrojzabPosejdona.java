package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class TrojzabPosejdona extends EventItem {
  private final NamespacedKey tempArrowKey;
  
  private double hitDamage;
  
  private double launchPower;
  
  private String hitTitle;
  
  private String hitSub;
  
  private String itemName;
  
  public TrojzabPosejdona(Main plugin) {
    super(plugin, "trojzab_posejdona");
    this.tempArrowKey = new NamespacedKey((Plugin)plugin, "temp_arrow");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.hitDamage = this.plugin.getConfig().getDouble("meta.trojzab_posejdona.hit_damage", 10.0D);
    this.launchPower = this.plugin.getConfig().getDouble("meta.trojzab_posejdona.launch_power", 2.5D);
    this.hitTitle = this.plugin.getMessagesConfig().getString("items.trojzab_posejdona.hit_title");
    this.hitSub = this.plugin.getMessagesConfig().getString("items.trojzab_posejdona.hit_subtitle");
    this.itemName = this.plugin.getConfig().getString("meta.trojzab_posejdona.name", "Trójząb Posejdona");
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT") && !hasArrows(player)) {
      ItemStack tempArrow = new ItemStack(Material.ARROW);
      ItemMeta meta = tempArrow.getItemMeta();
      meta.getPersistentDataContainer().set(this.tempArrowKey, PersistentDataType.BYTE, Byte.valueOf((byte)1));
      meta.setDisplayName(ChatUtils.color("&bMagiczna Strzała Posejdona"));
      tempArrow.setItemMeta(meta);
      player.getInventory().addItem(new ItemStack[] { tempArrow });
    } 
  }
  
  private boolean hasArrows(Player player) {
    for (ItemStack item : player.getInventory().getContents()) {
      if (item != null && (item.getType() == Material.ARROW || item.getType() == Material.SPECTRAL_ARROW || item.getType() == Material.TIPPED_ARROW))
        return true; 
    } 
    return false;
  }
  
  public void onLaunch(ProjectileLaunchEvent event, Player player, ItemStack item) {
    Projectile projectile = event.getEntity();
    if (projectile instanceof Arrow) {
      Arrow arrow = (Arrow)projectile;
      if (player.isSneaking()) {
        event.setCancelled(true);
        handleLaunch(player, item);
        return;
      } 
      if (isBlocked(player)) {
        event.setCancelled(true);
        return;
      } 
      String cooldownId = this.id + ".shot";
      if (checkCooldown(player, item, cooldownId)) {
        event.setCancelled(true);
        return;
      } 
      arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
      arrow.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
      applyUse(player, null, item, cooldownId);
    } 
  }
  
  public void onShootBow(EntityShootBowEvent event, Player player, ItemStack item) {
    ItemStack arrowItem = event.getConsumable();
    if (arrowItem != null && arrowItem.hasItemMeta() && arrowItem.getItemMeta().getPersistentDataContainer().has(this.tempArrowKey, PersistentDataType.BYTE))
      player.getInventory().remove(arrowItem.getType()); 
  }
  
  public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
    Location loc = (event.getHitEntity() != null) ? event.getHitEntity().getLocation() : ((event.getHitBlock() != null) ? event.getHitBlock().getLocation() : null);
    if (loc == null)
      return; 
    loc.getWorld().strikeLightningEffect(loc);
    loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
    loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 3);
    loc.getWorld().spawnParticle(Particle.WATER_DROP, loc, 50, 1.0D, 1.0D, 1.0D, 0.1D);
    Entity entity = event.getHitEntity();
    if (entity instanceof Player) {
      Player victim = (Player)entity;
      sendVictimNotification(victim, shooter);
    } 
    if (shooter != null && (this.hitTitle != null || this.hitSub != null)) {
      Player victimEntity = (event.getHitEntity() instanceof Player) ? (Player)event.getHitEntity() : null;
      shooter.sendTitle(ChatUtils.color(replacePlaceholders(this.hitTitle, shooter, victimEntity, this.itemName)), ChatUtils.color(replacePlaceholders(this.hitSub, shooter, victimEntity, this.itemName)), 5, 20, 5);
    } 
  }
  
  private void handleLaunch(Player player, ItemStack item) {
    if (isBlocked(player))
      return; 
    String cooldownId = this.id + ".launch";
    if (checkCooldown(player, item, cooldownId))
      return; 
    player.setVelocity(player.getLocation().getDirection().normalize().multiply(this.launchPower).setY(1.2D));
    player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.5F, 0.7F);
    player.getWorld().strikeLightningEffect(player.getLocation());
    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 30, 0.3D, 0.3D, 0.3D, 0.1D);
    applyUse(player, null, item, cooldownId);
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onTridentDamage(EntityDamageByEntityEvent event) {
    Arrow arrow;
    Player victim;
    Entity entity1 = event.getDamager();
    if (entity1 instanceof Arrow) {
      arrow = (Arrow)entity1;
    } else {
      return;
    } 
    if (!arrow.getPersistentDataContainer().has(this.itemKey, PersistentDataType.STRING))
      return; 
    if (!((String)arrow.getPersistentDataContainer().get(this.itemKey, PersistentDataType.STRING)).equals(this.id))
      return; 
    Entity entity2 = event.getEntity();
    if (entity2 instanceof Player) {
      victim = (Player)entity2;
    } else {
      return;
    } 
    if (this.plugin.getEventItemManager().isImmune((Entity)victim))
      return; 
    event.setDamage(0.01D);
    double newHealth = Math.max(0.0D, victim.getHealth() - 6.0D);
    victim.setHealth(newHealth);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\TrojzabPosejdona.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */