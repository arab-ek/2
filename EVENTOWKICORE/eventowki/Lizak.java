package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Lizak extends EventItem {
  public Lizak(Main plugin) {
    super(plugin, "lizak");
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT"))
      event.setCancelled(true); 
  }
  
  public void onBlockPlace(BlockPlaceEvent event, Player player, ItemStack item) {
    event.setCancelled(true);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\Lizak.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */