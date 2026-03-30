package dev.arab.TOOLS.SPRAWDZANIE;

import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckManager {
    private final Main plugin;
    private final ConfigSprawdzania config;
    private final Map<Player, Player> checkedPlayers = new HashMap<>(); // Kto jest sprawdzany -> Kto sprawdza

    public enum CheckResult {
        CZYSTY, BRAK_WSPOLPRACY, CHEATY, WYLOGOWANIE, PRZYZNANIE_SIE
    }

    public CheckManager(Main plugin, ConfigSprawdzania config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean isChecked(Player p) {
        return checkedPlayers.containsKey(p);
    }

    public boolean isAdminChecking(Player admin) {
        return checkedPlayers.containsValue(admin);
    }

    public Player getAdminChecking(Player target) {
        return checkedPlayers.get(target);
    }

    public void startCheck(Player admin, Player target) {
        checkedPlayers.put(target, admin);
        FileConfiguration cfg = config.getConfig();

        // Teleport do klatki
        World w = Bukkit.getWorld(cfg.getString("locations.cage.world", "world"));
        double x = cfg.getDouble("locations.cage.x");
        double y = cfg.getDouble("locations.cage.y");
        double z = cfg.getDouble("locations.cage.z");
        if (w != null) {
            target.teleport(new Location(w, x, y, z));
            admin.teleport(new Location(w, x, y, z));
        }

        target.sendTitle(config.getMessage("checking_target_title"), config.getMessage("checking_target_subtitle").replace("{ADMIN}", admin.getName()), 10, 70, 20);

        broadcastList(cfg.getStringList("messages.broadcasts.start_checking"), admin, target);
    }

    public void finishCheck(Player target, Player admin, CheckResult result) {
        checkedPlayers.remove(target);
        FileConfiguration cfg = config.getConfig();

        // Teleport na spawn (jeśli czysty)
        if (result == CheckResult.CZYSTY) {
            World w = Bukkit.getWorld(cfg.getString("locations.spawn.world", "world"));
            if (w != null) {
                Location spawn = new Location(w, cfg.getDouble("locations.spawn.x"), cfg.getDouble("locations.spawn.y"), cfg.getDouble("locations.spawn.z"));
                target.teleport(spawn);
                admin.teleport(spawn);
            }
            broadcastList(cfg.getStringList("messages.broadcasts.clear"), admin, target);
            return;
        }

        // Nakładanie banów
        String banCmd = "";
        switch (result) {
            case CHEATY:
                banCmd = cfg.getString("commands.ban_cheaty", "tempban {player} 7d &cCheaty");
                broadcastList(cfg.getStringList("messages.broadcasts.cheater"), admin, target);
                break;
            case BRAK_WSPOLPRACY:
                banCmd = cfg.getString("commands.ban_brak_wspolpracy", "tempban {player} 7d &cBrak wspolpracy");
                broadcastList(cfg.getStringList("messages.broadcasts.not_cooperated"), admin, target);
                break;
            case PRZYZNANIE_SIE:
                banCmd = cfg.getString("commands.ban_przyznanie", "tempban {player} 3d &cPrzyznanie sie do cheatow");
                broadcastList(cfg.getStringList("messages.broadcasts.admit"), admin, target);
                break;
            case WYLOGOWANIE:
                banCmd = cfg.getString("commands.ban_wylogowanie", "tempban {player} 7d &cWylogowanie sie");
                broadcastList(cfg.getStringList("messages.broadcasts.logout"), admin, target);
                break;
        }

        if (!banCmd.isEmpty()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCmd.replace("{player}", target.getName()));
        }
    }

    private void broadcastList(List<String> list, Player admin, Player target) {
        if (list == null) return;
        for (String line : list) {
            Bukkit.broadcastMessage(line.replace("&", "§")
                    .replace("{ADMIN}", admin.getName())
                    .replace("{TARGET}", target.getName()));
        }
    }
}