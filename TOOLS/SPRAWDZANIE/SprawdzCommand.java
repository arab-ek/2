package dev.arab.TOOLS.SPRAWDZANIE;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SprawdzCommand implements CommandExecutor, TabCompleter {
    private final CheckManager checkManager;
    private final ConfigSprawdzania config;

    public SprawdzCommand(CheckManager checkManager, ConfigSprawdzania config) {
        this.checkManager = checkManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("core.sprawdz")) {
            sender.sendMessage(config.getMessage("no_permission"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Komenda tylko dla graczy!");
            return true;
        }

        Player admin = (Player) sender;

        if (args.length < 2) {
            admin.sendMessage(config.getMessage("correct_usage").replace("{USAGE}", "/sprawdz [nick] <wezwij|czysty|brakwspolpracy|cheaty|wyloguj>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            admin.sendMessage(config.getMessage("null_player"));
            return true;
        }

        String action = args[1].toLowerCase();

        if (action.equals("wezwij")) {
            if (checkManager.isChecked(target)) {
                admin.sendMessage(config.getMessage("is_checking_target"));
                return true;
            }
            if (checkManager.isAdminChecking(admin)) {
                admin.sendMessage(config.getMessage("is_checking_admin"));
                return true;
            }
            checkManager.startCheck(admin, target);
            admin.sendMessage(config.getMessage("started_checking_admin").replace("{PLAYER}", target.getName()));
            return true;
        }

        if (!checkManager.isChecked(target)) {
            admin.sendMessage(config.getMessage("is_not_checking_target"));
            return true;
        }

        switch (action) {
            case "czysty":
                checkManager.finishCheck(target, admin, CheckManager.CheckResult.CZYSTY);
                break;
            case "brakwspolpracy":
                checkManager.finishCheck(target, admin, CheckManager.CheckResult.BRAK_WSPOLPRACY);
                break;
            case "cheaty":
                checkManager.finishCheck(target, admin, CheckManager.CheckResult.CHEATY);
                break;
            case "wyloguj":
                checkManager.finishCheck(target, admin, CheckManager.CheckResult.WYLOGOWANIE);
                break;
            default:
                admin.sendMessage(config.getMessage("correct_usage").replace("{USAGE}", "/sprawdz [nick] <wezwij|czysty|brakwspolpracy|cheaty|wyloguj>"));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("core.sprawdz")) return new ArrayList<>();

        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) names.add(online.getName());
            return names;
        } else if (args.length == 2) {
            return Arrays.asList("wezwij", "czysty", "brakwspolpracy", "cheaty", "wyloguj");
        }
        return new ArrayList<>();
    }
}