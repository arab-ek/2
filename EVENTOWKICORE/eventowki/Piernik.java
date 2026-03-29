package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Piernik extends EventItem {
  private int level;
  
  private int dur;
  
  public Piernik(Main plugin) {
    super(plugin, "piernik");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.level = this.plugin.getConfig().getInt("meta.piernik.haste_level", 10);
    this.dur = this.plugin.getConfig().getInt("meta.piernik.haste_duration", 30) * 20;
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      handlePiernik(player, item);
    } 
  }
  
  private void handlePiernik(Player player, ItemStack item) {
    if (isBlocked(player) || checkCooldown(player, item))
      return; 
    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, this.dur, this.level - 1));
    applyUse(player, null, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\Piernik.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */