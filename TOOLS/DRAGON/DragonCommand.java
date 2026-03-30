package dev.arab.TOOLS.DRAGON;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DragonCommand implements CommandExecutor {
    private final DragonBossManager manager;

    public DragonCommand(DragonBossManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("core.admin")) {
            sender.sendMessage("§cBrak uprawnien.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
            if (manager.getActiveDragon() != null) {
                sender.sendMessage("§cSmok juz zyje!");
                return true;
            }
            manager.startCountdown();
            sender.sendMessage("§aWymuszono start odliczania do spawnu smoka!");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            manager.cleanup();
            sender.sendMessage("§aWymuszono usuniecie smoka, hologramow i bossbara.");
            return true;
        }

        sender.sendMessage("§8» §fPoprawne uzycie: §d/boss <start|clear>");
        return true;
    }
}