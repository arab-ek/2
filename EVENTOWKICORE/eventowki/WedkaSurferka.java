package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class WedkaSurferka extends EventItem {
  private double pullPower;
  
  public WedkaSurferka(Main plugin) {
    super(plugin, "wedka_surferka");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.pullPower = this.plugin.getConfig().getDouble("meta.wedka_surferka.pull_power", 2.5D);
  }
  
  public void onFish(PlayerFishEvent event, Player player, ItemStack item) {
    if (event.getState() == PlayerFishEvent.State.REEL_IN || event.getState() == PlayerFishEvent.State.IN_GROUND || event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
      if (isBlocked(player) || checkCooldown(player, item))
        return; 
      Vector dir = event.getHook().getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
      player.setVelocity(dir.multiply(this.pullPower));
      applyUse(player, null, item, this.id);
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\WedkaSurferka.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */