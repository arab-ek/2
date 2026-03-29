package dev.arab.EVENTOWKICORE.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import dev.arab.EVENTOWKICORE.eventowki.TurboTrap;
import dev.arab.EVENTOWKICORE.inventory.EventInventory;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class AnarchiaEventowkiCommand implements CommandExecutor, TabCompleter {
  private final Main plugin;
  
  public AnarchiaEventowkiCommand(Main plugin) {
    this.plugin = plugin;
  }
  
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Ta komenda jest tylko dla graczy!");
      return true;
    } 
    Player player = (Player)sender;
    if (args.length >= 2 && args[0].equalsIgnoreCase("reset") && args[1].equalsIgnoreCase("cooldown")) {
      if (!player.hasPermission("anarchiaeventowki.resetcooldown")) {
        player.sendMessage(ChatUtils.color("&cNie masz uprawnien do tej komendy!"));
        return true;
      } 
      this.plugin.getCooldownManager().clearAllCooldowns(player.getUniqueId());
      TurboTrap.resetAll();
      player.sendMessage(ChatUtils.color("&aWszystkie cooldowny zostaly zresetowane!"));
      return true;
    } 
    if (!player.hasPermission("anarchiaeventowki.use")) {
      player.sendMessage(ChatUtils.color("&cNie masz uprawnien do tej komendy!"));
      return true;
    } 
    (new EventInventory(this.plugin)).open(player);
    return true;
  }
  
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    List<String> completions = new ArrayList<>();
    if (args.length == 1) {
      if (matchesPartial("reset", args[0]))
        completions.add("reset"); 
    } else if (args.length == 2 && args[0].equalsIgnoreCase("reset") && matchesPartial("cooldown", args[1])) {
      completions.add("cooldown");
    } 
    return Collections.unmodifiableList(completions);
  }
  
  private boolean matchesPartial(String full, String partial) {
    return full.toLowerCase().startsWith(partial.toLowerCase());
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\commands\AnarchiaEventowkiCommand.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */