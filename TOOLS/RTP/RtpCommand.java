package dev.arab.TOOLS.RTP;

import dev.arab.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RtpCommand implements CommandExecutor {
    private final RtpManager rtpManager;
    private final ConfigRtp config;

    public RtpCommand(Main plugin, ConfigRtp config, RtpManager rtpManager) {
        this.config = config;
        this.rtpManager = rtpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Komenda tylko dla graczy!");
            return true;
        }

        Player player = (Player) sender;

        if (config.getConfig().getBoolean("settings.require_permission") && !player.hasPermission("core.rtp")) {
            player.sendMessage(config.getMessage("no_permission"));
            return true;
        }

        // Wywołujemy teleportację z enforceCooldown = true
        rtpManager.performRtp(player, true);

        return true;
    }
}