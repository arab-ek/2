package dev.arab.ADDONS.INCOGNITO;

import dev.arab.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class IncognitoCommand implements CommandExecutor {
  private final Main plugin;
  
  private final IncognitoManager incognitoManager;
  
  public IncognitoCommand(Main plugin, IncognitoManager incognitoManager) {
    this.plugin = plugin;
    this.incognitoManager = incognitoManager;
  }
  
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    Player player;
    if (sender instanceof Player) {
      player = (Player)sender;
    } else {
      sender.sendMessage("Ta komenda jest tylko dla graczy!");
      return true;
    } 
    if (label.equalsIgnoreCase("incognito")) {
      if (!player.hasPermission("paczki.incognito")) {
        player.sendMessage("§cNie masz do tego permisji!");
        return true;
      } 
      this.plugin.getModuleInventory().openInventory(player);
      return true;
    } 
    return false;
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\INCOGNITO\IncognitoCommand.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */