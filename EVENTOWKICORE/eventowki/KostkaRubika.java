package dev.arab.EVENTOWKICORE.eventowki;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class KostkaRubika extends EventItem {
  private String blockTitle;
  
  private String blockSub;
  
  public KostkaRubika(Main plugin) {
    super(plugin, "kostka_rubika");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.blockTitle = ChatUtils.color(this.plugin.getMessagesConfig().getString("messages.blocked_placement_title", "&#FF0000✘"));
    this.blockSub = ChatUtils.color(this.plugin.getMessagesConfig().getString("messages.blocked_placement_subtitle", "&#EC0000Nie możesz tego postawić!"));
  }
  
  public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
    if (isBlocked(damager) || checkCooldown(damager, item))
      return; 
    scrambleInventory(victim);
    sendVictimNotification(victim, damager);
    applyUse(damager, victim, item, this.id);
  }
  
  private void scrambleInventory(Player player) {
    List<ItemStack> items = new ArrayList<>();
    PlayerInventory inv = player.getInventory();
    int i;
    for (i = 0; i < 9; i++)
      items.add(inv.getItem(i)); 
    Collections.shuffle(items);
    for (i = 0; i < 9; i++)
      inv.setItem(i, items.get(i)); 
  }
  
  public void onBlockPlace(BlockPlaceEvent event, Player player, ItemStack item) {
    event.setCancelled(true);
    player.sendTitle(this.blockTitle, this.blockSub, 5, 20, 5);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\KostkaRubika.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */