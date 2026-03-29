package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.EVENTOWKICORE.eventowki.EventItem;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;

public class ArcusMagnus extends EventItem {
  private final String COMBO_METADATA = "arcus_magnus_combo";
  private double comboBonusDamage;
  private int maxCombo;
  private String titleTemplate;
  private String subTemplate;
  private String itemName;

  public ArcusMagnus(Main plugin) {
    super(plugin, "arcus_magnus");
  }

  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.comboBonusDamage = this.plugin.getConfig().getDouble("meta.arcus_magnus.combo_bonus_damage", 2.0D);
    this.maxCombo = this.plugin.getConfig().getInt("meta.arcus_magnus.max_combo", 5);
    this.titleTemplate = this.plugin.getMessagesConfig().getString("items.arcus_magnus.use_title", "&a&lCOMBO");
    this.subTemplate = this.plugin.getMessagesConfig().getString("items.arcus_magnus.use_subtitle", "&7Trafienie &e%combo%&7/&e%max_combo%&7! (+%bonus% dmg)");
    this.itemName = this.plugin.getConfig().getString("meta.arcus_magnus.name", "Arcus Magnus");
  }

  public void onLaunch(ProjectileLaunchEvent event, Player player, ItemStack item) {
    Projectile var5 = event.getEntity();
    if (var5 instanceof Arrow) {
      Arrow arrow = (Arrow)var5;
      if (this.isBlocked(player) || this.checkCooldown(player, item)) {
        event.setCancelled(true);
        return;
      }

      arrow.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
      this.applyUseSilent(player, (Player)null, item, this.id);
    }

  }

  public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
    if (shooter != null) {
      Entity var4 = event.getHitEntity();
      if (var4 instanceof Player) {
        Player victim = (Player)var4;
        this.handleHit(shooter, victim);
      } else if (event.getHitBlock() != null) {
        this.resetCombo(shooter);
      }

    }
  }

  private void handleHit(Player shooter, Player victim) {
    int combo = 0;
    if (shooter.hasMetadata("arcus_magnus_combo")) {
      combo = ((MetadataValue)shooter.getMetadata("arcus_magnus_combo").get(0)).asInt();
    }

    combo = Math.min(combo + 1, this.maxCombo);
    shooter.setMetadata("arcus_magnus_combo", new FixedMetadataValue(this.plugin, combo));
    double totalBonus = (double)Math.max(2, combo) * this.comboBonusDamage;
    if (totalBonus > 0.0D) {
      victim.damage(totalBonus, shooter);
    }

    String sub = this.subTemplate != null ? this.subTemplate.replace("%combo%", String.valueOf(combo)).replace("%max_combo%", String.valueOf(this.maxCombo)).replace("%bonus%", String.format("%.1f", totalBonus)) : null;
    shooter.sendTitle(ChatUtils.color(this.replacePlaceholders(this.titleTemplate, shooter, victim, this.itemName)), ChatUtils.color(this.replacePlaceholders(sub, shooter, victim, this.itemName)), 5, 20, 5);
    this.sendVictimNotification(victim, shooter);
  }

  private void resetCombo(Player shooter) {
    if (shooter.hasMetadata("arcus_magnus_combo")) {
      shooter.removeMetadata("arcus_magnus_combo", this.plugin);
      shooter.sendMessage(ChatUtils.color("&cTwoje combo zostalo przerwane!"));
    }

  }
}