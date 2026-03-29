package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class KrewWampira extends EventItem {
  public KrewWampira(Main plugin) {
    super(plugin, "krew_wampira");
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      if (isBlocked(player) || checkCooldown(player, item))
        return; 
      player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
      player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0.0D, 1.0D, 0.0D), 5, 0.2D, 0.2D, 0.2D, 0.1D);
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
      applyUse(player, null, item, this.id);
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\KrewWampira.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */