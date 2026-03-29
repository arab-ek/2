package dev.arab.EVENTOWKICORE.commands;

import dev.arab.EVENTOWKICORE.eventowki.EventItem;
import dev.arab.EVENTOWKICORE.eventowki.Excalibur;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ExcaliburCommand implements CommandExecutor {
  private final Main plugin;
  
  private final NamespacedKey itemKey;
  
  private final NamespacedKey killsKey;
  
  public ExcaliburCommand(Main plugin) {
    this.plugin = plugin;
    this.itemKey = new NamespacedKey((Plugin)plugin, "event_item_id");
    this.killsKey = new NamespacedKey((Plugin)plugin, "excalibur_kills");
  }
  
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    int amount;
    if (!sender.hasPermission("core.command.excalibur")) {
      sender.sendMessage(ChatUtils.color("&cNie masz do tego permisji!"));
      return true;
    } 
    if (args.length < 3 || !args[0].equalsIgnoreCase("add")) {
      sender.sendMessage(ChatUtils.color("&cUzycie: &4/excalibur add <gracz> <ilosc>"));
      return true;
    } 
    Player target = Bukkit.getPlayer(args[1]);
    if (target == null) {
      sender.sendMessage(ChatUtils.color("&cGracz offline!"));
      return true;
    } 
    try {
      amount = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
      sender.sendMessage(ChatUtils.color("&cPodaj poprawna liczbe!"));
      return true;
    } 
    ItemStack item = target.getInventory().getItemInMainHand();
    if (item == null || !item.hasItemMeta()) {
      sender.sendMessage(ChatUtils.color("&cGracz nic nie trzyma!"));
      return true;
    } 
    ItemMeta meta = item.getItemMeta();
    String id = (String)meta.getPersistentDataContainer().get(this.itemKey, PersistentDataType.STRING);
    if (id == null || !id.equals("excalibur")) {
      sender.sendMessage(ChatUtils.color("&cTo nie jest Excalibur!"));
      return true;
    } 
    int currentKills = ((Integer)meta.getPersistentDataContainer().getOrDefault(this.killsKey, PersistentDataType.INTEGER, Integer.valueOf(0))).intValue();
    int newKills = currentKills + amount;
    meta.getPersistentDataContainer().set(this.killsKey, PersistentDataType.INTEGER, Integer.valueOf(newKills));
    item.setItemMeta(meta);
    EventItem eventItem = this.plugin.getEventItemManager().getItemById("excalibur");
    if (eventItem instanceof Excalibur) {
      Excalibur excalibur = (Excalibur)eventItem;
      excalibur.updateExcaliburLore(item, newKills, currentKills);
    } 
    sender.sendMessage(ChatUtils.color("&aDodano " + amount + " killi do Excalibura gracza " + target.getName()));
    target.sendMessage(ChatUtils.color("&aAdministrator dodal kille do Twojego Excalibura!"));
    return true;
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\commands\ExcaliburCommand.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */