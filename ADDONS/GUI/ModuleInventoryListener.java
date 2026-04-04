package dev.arab.ADDONS.GUI;

import dev.arab.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ModuleInventoryListener implements Listener {
  private final Main plugin;
  
  private final ModuleInventory moduleInventory;
  
  public ModuleInventoryListener(Main plugin, ModuleInventory moduleInventory) {
    this.plugin = plugin;
    this.moduleInventory = moduleInventory;
  }
  
  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    Player player;
    HumanEntity humanEntity = event.getWhoClicked();
    if (humanEntity instanceof Player) {
      player = (Player)humanEntity;
    } else {
      return;
    } 
    if (event.getClickedInventory() == null)
      return; 
    if (!(event.getView().getTopInventory().getHolder() instanceof ModuleGuiHolder))
      return; 
    event.setCancelled(true);
    int slot = event.getSlot();
    int incognitoSlot = this.moduleInventory.getConfig().getInt("gui.items.incognito.slot");
    int creatorSlot = this.moduleInventory.getConfig().getInt("gui.items.trybtworcy.slot");
    if (slot == incognitoSlot) {
      if (!player.hasPermission("paczki.incognito")) {
        String noPerm = this.plugin.getTrybTworcyManager().getConfig().getString("messages.no_permission", "§cNie masz do tego permisji!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', noPerm));
        return;
      } 
      this.plugin.getIncognitoManager().toggleIncognito(player);
      this.moduleInventory.setupItems(event.getClickedInventory(), player);
    } else if (slot == creatorSlot) {
      if (!player.hasPermission("trybtworcy.use")) {
        String noPerm = this.plugin.getTrybTworcyManager().getConfig().getString("messages.no_permission", "§cNie masz do tego permisji!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', noPerm));
        return;
      } 
      this.plugin.getTrybTworcyManager().toggleMode(player);
      this.moduleInventory.setupItems(event.getClickedInventory(), player);
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\GUI\ModuleInventoryListener.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */