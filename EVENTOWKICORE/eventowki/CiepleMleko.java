package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CiepleMleko extends EventItem {
  private String noEffectsMsg;
  
  public CiepleMleko(Main plugin) {
    super(plugin, "cieple_mleko");
  }
  
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.noEffectsMsg = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.cieple_mleko.no_negative_effects", "&cMusisz miec negatywne efekty!"));
  }
  
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      handleCiepleMleko(player, item);
    } 
  }
  
  private void handleCiepleMleko(Player player, ItemStack item) {
    if (isBlocked(player) || checkCooldown(player, item))
      return; 
    boolean removed = false;
    for (PotionEffect effect : player.getActivePotionEffects()) {
      if (isNegative(effect.getType())) {
        player.removePotionEffect(effect.getType());
        removed = true;
      } 
    } 
    if (removed) {
      applyUse(player, null, item, this.id);
    } else {
      player.sendMessage(this.noEffectsMsg);
    } 
  }
  
  private boolean isNegative(PotionEffectType type) {
    String n = type.getName();
    return (n.equals("BLINDNESS") || n.equals("CONFUSION") || n.equals("HARM") || n.equals("HUNGER") || n.equals("POISON") || n.equals("SLOW") || n.equals("SLOW_DIGESTING") || n.equals("WEAKNESS") || n.equals("WITHER") || n.equals("LEVITATION") || n.equals("UNLUCK") || n.equals("GLOWING") || n.equals("BAD_OMEN"));
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\eventowki\CiepleMleko.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */