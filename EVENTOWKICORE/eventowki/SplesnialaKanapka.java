package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SplesnialaKanapka extends EventItem {
  private int dur;
  
  private int level;
  
  public SplesnialaKanapka(Main plugin) {
    super(plugin, "splesniala_kanapka");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.dur = this.plugin.getConfig().getInt("meta.splesniala_kanapka.disease_duration", 10) * 20;
    this.level = this.plugin.getConfig().getInt("meta.splesniala_kanapka.disease_level", 1) - 1;
  }
  
  public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
    if (isBlocked(damager) || checkCooldown(damager, item))
      return; 
    victim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, this.dur, this.level));
    victim.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, this.dur, this.level));
    sendVictimNotification(victim, damager);
    applyUse(damager, victim, item, this.id);
  }
  
  public void onConsume(PlayerItemConsumeEvent event, Player player, ItemStack item) {
    event.setCancelled(true);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\SplesnialaKanapka.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */