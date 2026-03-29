package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class RozaKupidyna extends EventItem {
  public RozaKupidyna(Main plugin) {
    super(plugin, "roza_kupidyna");
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      if (isBlocked(player) || checkCooldown(player, item))
        return; 
      applyUse(player, null, item, this.id);
    } 
  }
  
  public void onBlockPlace(BlockPlaceEvent event, Player player, ItemStack item) {
    event.setCancelled(true);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\RozaKupidyna.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */