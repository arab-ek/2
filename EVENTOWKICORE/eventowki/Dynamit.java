package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Dynamit extends EventItem {
  private String errorMsg;
  
  public Dynamit(Main plugin) {
    super(plugin, "dynamit");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.errorMsg = ChatUtils.color(this.plugin.getMessagesConfig().getString("bedrock_destroy_error", "&cNie mozesz niszczyc naturalnego bedrocka!"));
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      event.setCancelled(true);
      handleDynamit(player, item, event.getClickedBlock());
    } 
  }
  
  private void handleDynamit(Player player, ItemStack item, Block block) {
    if (isBlocked(player) || checkCooldown(player, item))
      return; 
    if (block == null || block.getType() != Material.BEDROCK)
      return; 
    if (!this.plugin.getBlockTracker().isPlacedBlock(block.getLocation())) {
      player.sendMessage(this.errorMsg);
      return;
    } 
    block.setType(Material.AIR);
    this.plugin.getBlockTracker().removeBlock(block.getLocation());
    block.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, block.getLocation().add(0.5D, 0.5D, 0.5D), 5);
    block.getWorld().playSound(block.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.2F);
    applyUse(player, null, item, this.id);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\Dynamit.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */