package dev.arab.TOOLS.SPRAWDZANIE;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CheckController implements Listener {
    private final CheckManager checkManager;
    private final ConfigSprawdzania config;

    public CheckController(CheckManager checkManager, ConfigSprawdzania config) {
        this.checkManager = checkManager;
        this.config = config;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (checkManager.isChecked(p)) {
            String cmd = e.getMessage().toLowerCase().split(" ")[0];

            if (cmd.equals("/przyznajsie") || cmd.equals("/przyznajesie")) {
                return;
            }

            e.setCancelled(true);
            p.sendMessage(config.getMessage("blocked_command"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (checkManager.isChecked(p)) {
            Player admin = checkManager.getAdminChecking(p);
            checkManager.finishCheck(p, admin != null ? admin : p, CheckManager.CheckResult.WYLOGOWANIE);
        }
    }

    @EventHandler
    public void onDamageTake(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (checkManager.isChecked(p)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamageDeal(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            if (checkManager.isChecked(damager)) {
                e.setCancelled(true);
            }
        }
    }
}