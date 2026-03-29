package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class WzmocnionaElytra extends EventItem {
  private final NamespacedKey chargeKey;
  
  private double strikeRad;
  
  private double strikeDmg;
  
  private boolean blastDmg;
  
  public WzmocnionaElytra(Main plugin) {
    super(plugin, "wzmocniona_elytra");
    this.chargeKey = new NamespacedKey((Plugin)plugin, "elytra_charge");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.strikeRad = this.plugin.getConfig().getDouble("meta.wzmocniona_elytra.strike_radius", 5.0D);
    this.strikeDmg = this.plugin.getConfig().getDouble("meta.wzmocniona_elytra.strike_damage", 6.0D);
    this.blastDmg = this.plugin.getConfig().getBoolean("meta.wzmocniona_elytra.block_damage", false);
  }
  
  public void onMove(PlayerMoveEvent event, Player player) {
    if (!player.isOnGround() || player.isGliding())
      return; 
    ItemStack chest = player.getInventory().getChestplate();
    if (chest == null || chest.getType() != Material.ELYTRA || !chest.hasItemMeta())
      return; 
    ItemMeta meta = chest.getItemMeta();
    if (!isCustomItem(chest, meta))
      return; 
    double charge = ((Double)meta.getPersistentDataContainer().getOrDefault(this.chargeKey, PersistentDataType.DOUBLE, Double.valueOf(0.0D))).doubleValue();
    if (charge >= 100.0D)
      handleElytraImpact(player, chest); 
    if (charge > 0.0D) {
      meta.getPersistentDataContainer().set(this.chargeKey, PersistentDataType.DOUBLE, Double.valueOf(0.0D));
      chest.setItemMeta(meta);
    } 
  }
  
  public void onElytraToggle(EntityToggleGlideEvent event, Player player, ItemStack chest) {}
  
  private void handleElytraImpact(Player player, ItemStack item) {
    if (checkCooldown(player, item))
      return; 
    Location loc = player.getLocation();
    loc.getWorld().strikeLightningEffect(loc);
    loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 3, 0.5D, 0.5D, 0.5D, 0.1D);
    loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5F, 0.8F);
    for (Entity e : loc.getWorld().getNearbyEntities(loc, this.strikeRad, this.strikeRad, this.strikeRad)) {
      if (e instanceof LivingEntity) {
        LivingEntity le = (LivingEntity)e;
        if (!le.equals(player)) {
          double newHealth = le.getHealth() - this.strikeDmg;
          le.getWorld().strikeLightningEffect(le.getLocation());
          if (le instanceof Player) {
            Player p = (Player)le;
            if (newHealth <= 0.0D) {
              p.setHealth(0.5D);
              p.setNoDamageTicks(0);
              p.damage(le.getMaxHealth() * 10.0D, (Entity)player);
            } else {
              p.setHealth(newHealth);
              p.setNoDamageTicks(0);
              p.damage(0.01D, (Entity)player);
            } 
            sendVictimNotification(p, player);
            continue;
          } 
          le.setHealth(Math.max(0.0D, newHealth));
          le.setNoDamageTicks(0);
          le.damage(0.01D, (Entity)player);
        } 
      } 
    } 
    if (this.blastDmg)
      loc.getWorld().createExplosion(loc, 0.0F, false, false); 
    applyUse(player, null, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\WzmocnionaElytra.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */