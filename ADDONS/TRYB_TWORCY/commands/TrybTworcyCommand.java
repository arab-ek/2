package dev.arab.ADDONS.TRYB_TWORCY.commands;

import java.util.ArrayList;
import java.util.List;
import dev.arab.Main;
import dev.arab.ADDONS.TRYB_TWORCY.managers.TrybTworcyManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class TrybTworcyCommand implements CommandExecutor, TabCompleter {
  private final Main plugin;
  private final TrybTworcyManager manager;

  public TrybTworcyCommand(Main plugin, TrybTworcyManager manager) {
    this.plugin = plugin;
    this.manager = manager;
  }

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
      if (!sender.hasPermission("trybtworcy.reload")) {
        String noPermMsg = this.manager.getConfig().getString("messages.no_permission", "&cNie do tego permisji!");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermMsg));
        return true;
      }
      this.manager.loadConfig();
      String reloadedMsg = this.manager.getConfig().getString("messages.reloaded", "&aKonfiguracja pluginu została przeładowana!");
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadedMsg));
      return true;
    }
    if (!(sender instanceof Player)) {
      String consoleMsg = this.manager.getConfig().getString("messages.only_player", "Tylko gracz może użyć tej komendy.");
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', consoleMsg));
      return true;
    }

    Player player = (Player)sender;
    if (!player.hasPermission("trybtworcy.use")) {
      String noPermMsg = this.manager.getConfig().getString("messages.no_permission", "&cNie do tego permisji!");
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermMsg));
      return true;
    }
    this.plugin.getModuleInventory().openInventory(player);
    return true;
  }

  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    List<String> completions = new ArrayList<>();
    if (args.length == 1 && sender.hasPermission("trybtworcy.reload") && "reload".startsWith(args[0].toLowerCase())) {
      completions.add("reload");
    }
    return completions;
  }
}