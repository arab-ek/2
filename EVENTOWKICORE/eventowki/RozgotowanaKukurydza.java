package dev.arab.EVENTOWKICORE.eventowki;

import java.util.Random;
import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class RozgotowanaKukurydza extends EventItem {
  private double explosionRadius;
  
  private int armorDamage;
  
  private double healthDamage;
  
  public RozgotowanaKukurydza(Main plugin) {
    super(plugin, "rozgotowana_kukurydza");
  }
  
  public boolean isBlockedByCage() {
    return true;
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.explosionRadius = this.plugin.getConfig().getDouble("meta.rozgotowana_kukurydza.explosion_radius", 6.0D);
    this.armorDamage = this.plugin.getConfig().getInt("meta.rozgotowana_kukurydza.armor_damage", 50);
    this.healthDamage = this.plugin.getConfig().getDouble("meta.rozgotowana_kukurydza.health_damage", 6.0D);
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      if (isBlocked(player) || checkCooldown(player, item))
        return; 
      if (HydroKlatka.isInsideCage(player.getLocation())) {
        sendBlockedNotification(player);
        return;
      } 
      Snowball snowball = (Snowball)player.launchProjectile(Snowball.class);
      snowball.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
      player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1.0F, 1.0F);
      applyUse(player, null, item, this.id);
    } 
  }
  
  public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
    Location loc = (event.getHitEntity() != null) ? event.getHitEntity().getLocation() : ((event.getHitBlock() != null) ? event.getHitBlock().getLocation() : event.getEntity().getLocation());
    if (HydroKlatka.isInsideCage(loc)) {
      if (shooter != null)
        sendBlockedNotification(shooter); 
      return;
    } 
    loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 3, 1.0D, 1.0D, 1.0D, 0.1D);
    loc.getWorld().spawnParticle(Particle.FLAME, loc, 20, 0.5D, 0.5D, 0.5D, 0.05D);
    loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.2F);
    int blockRadius = (int)Math.ceil(this.explosionRadius / 2.0D);
    Random random = this.plugin.getRandom();
    for (int x = -blockRadius; x <= blockRadius; x++) {
      for (int y = -blockRadius; y <= blockRadius; y++) {
        for (int z = -blockRadius; z <= blockRadius; z++) {
          Location target = loc.clone().add(x, y, z);
          if (loc.distanceSquared(target) <= blockRadius * (0.7D + random.nextDouble() * 0.6D) * blockRadius * (0.7D + random.nextDouble() * 0.6D)) {
            Block block = target.getBlock();
            Material type = block.getType();
            if (type != Material.BEDROCK && type != Material.AIR && type != Material.OBSIDIAN && type != Material.CRYING_OBSIDIAN && type != Material.RESPAWN_ANCHOR && type != Material.ANCIENT_DEBRIS && type != Material.ENCHANTING_TABLE && type != Material.ENDER_CHEST && type != Material.ANVIL && type != Material.CHIPPED_ANVIL && type != Material.DAMAGED_ANVIL)
              block.setType(Material.AIR); 
          } 
        } 
      } 
    } 
    for (Entity entity : loc.getWorld().getNearbyEntities(loc, this.explosionRadius, this.explosionRadius, this.explosionRadius)) {
      if (entity instanceof LivingEntity) {
        LivingEntity victim = (LivingEntity)entity;
        if (!victim.equals(shooter)) {
          victim.damage(this.healthDamage, (Entity)shooter);
          if (victim instanceof Player) {
            Player victimPlayer = (Player)victim;
            damageArmor(victimPlayer, this.armorDamage);
            sendVictimNotification(victimPlayer, shooter);
          } 
        } 
      } 
    } 
  }
  
  private void damageArmor(Player player, int amount) {
    ItemStack[] armor = player.getInventory().getArmorContents();
    boolean changed = false;
    for (ItemStack piece : armor) {
      if (piece != null && piece.getType() != Material.AIR && piece.hasItemMeta()) {
        ItemMeta itemMeta = piece.getItemMeta();
        if (itemMeta instanceof Damageable) {
          Damageable meta = (Damageable)itemMeta;
          EventItem item = this.plugin.getEventItemManager().getItem(piece);
          if (item == null || !item.getId().equals("korona_anarchii")) {
            meta.setDamage(meta.getDamage() + amount);
            piece.setItemMeta((ItemMeta)meta);
            if (meta.getDamage() >= piece.getType().getMaxDurability())
              piece.setAmount(0); 
            changed = true;
          } 
        } 
      } 
    } 
    if (changed)
      player.getInventory().setArmorContents(armor); 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\RozgotowanaKukurydza.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */