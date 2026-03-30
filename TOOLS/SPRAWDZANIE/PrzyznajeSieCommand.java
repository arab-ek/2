package dev.arab.TOOLS.SPRAWDZANIE;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrzyznajeSieCommand implements CommandExecutor {
    private final CheckManager checkManager;
    private final ConfigSprawdzania config;

    public PrzyznajeSieCommand(CheckManager checkManager, ConfigSprawdzania config) {
        this.checkManager = checkManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Komenda tylko dla graczy!");
            return true;
        }

        Player target = (Player) sender;

        if (!checkManager.isChecked(target)) {
            target.sendMessage(config.getMessage("admit_error"));
            return true;
        }

        Player admin = checkManager.getAdminChecking(target);
        checkManager.finishCheck(target, admin != null ? admin : target, CheckManager.CheckResult.PRZYZNANIE_SIE);
        return true;
    }
}