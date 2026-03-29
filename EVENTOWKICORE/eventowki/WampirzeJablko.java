/* Decompiler 81ms, total 281ms, lines 40 */
package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WampirzeJablko extends EventItem {
  private int strDur;

  public WampirzeJablko(Main plugin) {
    super(plugin, "wampirze_jablko");
  }

  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.strDur = this.plugin.getConfig().getInt("meta.wampirze_jablko.strength_duration", 15) * 20;
  }

  public void onConsume(PlayerItemConsumeEvent event, Player player, ItemStack item) {
    if (!this.isBlocked(player) && !this.checkCooldown(player, item)) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, this.strDur, 1));
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
      player.spawnParticle(Particle.DAMAGE_INDICATOR, player.getLocation().add(0.0D, 1.0D, 0.0D), 10, 0.5D, 0.5D, 0.5D, 0.1D);
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
      }, 1L);
      this.applyUse(player, (Player)null, item, this.id);
    } else {
      event.setCancelled(true);
    }
  }
}