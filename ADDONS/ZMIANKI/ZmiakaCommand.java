package dev.arab.ADDONS.ZMIANKI;

import java.util.ArrayList;
import java.util.List;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZmiakaCommand implements CommandExecutor, TabCompleter {
  private final Main plugin;
  
  private final ConfigZaczarowania config;
  
  public ZmiakaCommand(Main plugin, ConfigZaczarowania config) {
    this.plugin = plugin;
    this.config = config;
  }
  
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    int amount;
    if (!sender.hasPermission("zaczarowanie.admin")) {
      sender.sendMessage(ChatUtils.color("&cNie masz do tego permisji!"));
      return true;
    } 
    if (args.length < 3 || !args[0].equalsIgnoreCase("daj")) {
      sender.sendMessage(ChatUtils.color("&c/azmianki daj <nick> <ilość>"));
      return true;
    } 
    Player target = Bukkit.getPlayer(args[1]);
    if (target == null) {
      sender.sendMessage(ChatUtils.color("&cNie znaleziono gracza o takim nicku!"));
      return true;
    } 
    try {
      amount = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
      sender.sendMessage(ChatUtils.color("&cPodaj poprawną ilość!"));
      return true;
    } 
    ItemStack item = this.config.getItemFromConfig("enchant_item");
    item.setAmount(amount);
    target.getInventory().addItem(new ItemStack[] { item });
    sender.sendMessage(ChatUtils.color("&aNadano &2" + amount + "x &aprzedmiotów zaczarowania dla &2" + target.getName()));
    return true;
  }
  
  @Nullable
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    return (args.length == 1) ? List.of("daj") : ((args.length == 2) ? null : new ArrayList<>());
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\ZMIANKI\ZmiakaCommand.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */