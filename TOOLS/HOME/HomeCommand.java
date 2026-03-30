package dev.arab.TOOLS.HOME;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    private final HomeManager homeManager;
    private final ConfigHome config;
    private final HomeGui homeGui;

    public HomeCommand(HomeManager homeManager, ConfigHome config, HomeGui homeGui) {
        this.homeManager = homeManager;
        this.config = config;
        this.homeGui = homeGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Tylko dla graczy!");
            return true;
        }
        Player player = (Player) sender;
        String cmdName = command.getName().toLowerCase();

        if (cmdName.equals("sethome")) {
            String homeName = args.length > 0 ? args[0].toLowerCase() : "dom";
            int currentHomes = homeManager.getHomes(player.getUniqueId()).size();
            int limit = homeManager.getHomeLimit(player);

            // Jeśli ma już max domów i próbuje ustawić dom o innej nazwie
            if (currentHomes >= limit && !homeManager.getHomes(player.getUniqueId()).containsKey(homeName)) {
                player.sendMessage(config.getMessage("limit_reached").replace("{LIMIT}", String.valueOf(limit)));
                return true;
            }

            homeManager.setHome(player.getUniqueId(), homeName, player.getLocation());
            player.sendMessage(config.getMessage("home_set").replace("{NAME}", homeName));
            return true;
        }

        if (cmdName.equals("delhome")) {
            if (args.length < 1) {
                player.sendMessage(config.getMessage("correct_usage_delhome"));
                return true;
            }
            String homeName = args[0].toLowerCase();
            if (homeManager.getHome(player.getUniqueId(), homeName) == null) {
                player.sendMessage(config.getMessage("home_not_exists"));
                return true;
            }
            homeManager.deleteHome(player.getUniqueId(), homeName);
            player.sendMessage(config.getMessage("home_deleted").replace("{NAME}", homeName));
            return true;
        }

        if (cmdName.equals("home")) {
            if (args.length == 0) {
                homeGui.openGui(player); // Otwiera menu (GUI)
                return true;
            }
            String homeName = args[0].toLowerCase();
            org.bukkit.Location loc = homeManager.getHome(player.getUniqueId(), homeName);
            if (loc == null) {
                player.sendMessage(config.getMessage("home_not_exists"));
                return true;
            }
            player.teleport(loc);
            player.sendMessage(config.getMessage("teleported").replace("{NAME}", homeName));
            return true;
        }

        return true;
    }
}