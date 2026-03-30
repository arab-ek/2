package dev.arab.TOOLS.AFK;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class AfkZoneController implements Listener {
    private final Main plugin;
    private final ConfigAfk config;
    private final Map<UUID, AfkSession> sessions = new ConcurrentHashMap<>();
    private RegionQuery regionQuery;

    public AfkZoneController(Main plugin, ConfigAfk config) {
        this.plugin = plugin;
        this.config = config;

        try {
            this.regionQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().warning("WorldGuard nie zostal znaleziony! System AFK nie bedzie dzialac.");
        }

        startAfkTask();
    }

    private void startAfkTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!config.getConfig().getBoolean("settings.afk_zone.enabled")) return;
                if (regionQuery == null) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isInAfkRegion(player)) {
                        handleAfkProgress(player);
                    } else {
                        removePlayerFromAfk(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private boolean isInAfkRegion(Player player) {
        try {
            ApplicableRegionSet set = regionQuery.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
            for (ProtectedRegion region : set) {
                if (region.getId().equalsIgnoreCase(config.getConfig().getString("settings.afk_zone.region_name"))) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void handleAfkProgress(Player player) {
        UUID uuid = player.getUniqueId();
        AfkSession session = sessions.computeIfAbsent(uuid, k -> {
            player.sendMessage(config.getMessage("afk_enabled"));
            updateVisibility(player, true);

            BossBar stdBar = Bukkit.createBossBar("", parseBarColor(config.getConfig().getString("settings.afk_zone.standard.bossbar_color")), parseBarOverlay(config.getConfig().getString("settings.afk_zone.standard.bossbar_style")));
            BossBar premBar = Bukkit.createBossBar("", parseBarColor(config.getConfig().getString("settings.afk_zone.premium.bossbar_color")), parseBarOverlay(config.getConfig().getString("settings.afk_zone.premium.bossbar_style")));

            stdBar.addPlayer(player);
            premBar.addPlayer(player);

            return new AfkSession(stdBar, premBar);
        });

        // --- SPRAWDZANIE PETA "LENIUSZEK" ---
        // Domyślna wartość progresu to 1 sekunda na tick zadania.
        int progressToAdd = 1;

        if (plugin.getPetManager() != null) {
            String activePet = plugin.getPetManager().getEquippedPet(player);

            // Jeżeli ma założonego leniuszka, czas leci podwójnie!
            if (activePet != null && activePet.equalsIgnoreCase("leniuszek")) {
                progressToAdd = 2;
            }
        }

        // STANDARD PROGRESS
        session.standardProgress += progressToAdd;
        updateStandardBar(session);
        if (session.standardProgress >= config.getConfig().getInt("settings.afk_zone.standard.reward_seconds")) {
            giveStandardReward(player);
            session.standardProgress = 0;
        }

        // PREMIUM PROGRESS
        session.premiumProgress += progressToAdd;
        updatePremiumBar(player, session);
        if (session.premiumProgress >= config.getConfig().getInt("settings.afk_zone.premium.reward_seconds")) {
            givePremiumReward(player);
            session.premiumProgress = 0;
        }
    }

    private void updateStandardBar(AfkSession session) {
        int max = config.getConfig().getInt("settings.afk_zone.standard.reward_seconds");
        float progress = Math.min(1.0f, (float) session.standardProgress / max);
        int timeLeft = max - session.standardProgress;

        String title = config.getMessage("standard_reward_title")
                .replace("{TIME}", formatTime(timeLeft))
                .replace("{PERCENTAGE}", String.valueOf((int) (progress * 100)));

        session.standardBar.setTitle(title);
        session.standardBar.setProgress(progress);
    }

    private void updatePremiumBar(Player player, AfkSession session) {
        int max = config.getConfig().getInt("settings.afk_zone.premium.reward_seconds");
        float progress = Math.min(1.0f, (float) session.premiumProgress / max);
        int timeLeft = max - session.premiumProgress;
        double chance = getPremiumChance(player);

        String title = config.getMessage("premium_reward_title")
                .replace("{CHANCE}", String.valueOf(chance))
                .replace("{TIME}", formatTime(timeLeft))
                .replace("{PERCENTAGE}", String.valueOf((int) (progress * 100)));

        session.premiumBar.setTitle(title);
        session.premiumBar.setProgress(progress);
    }

    private void removePlayerFromAfk(Player player) {
        AfkSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            player.sendMessage(config.getMessage("afk_disabled"));
            session.standardBar.removePlayer(player);
            session.premiumBar.removePlayer(player);
            updateVisibility(player, false);
        }
    }

    private void updateVisibility(Player player, boolean hide) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            if (hide) {
                player.hidePlayer(plugin, online);
                online.hidePlayer(plugin, player);
            } else {
                player.showPlayer(plugin, online);
                online.showPlayer(plugin, player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayerFromAfk(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(joined)) continue;
            if (sessions.containsKey(online.getUniqueId())) {
                online.hidePlayer(plugin, joined);
                joined.hidePlayer(plugin, online);
            }
        }
    }

    public void cleanup() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AfkSession session = sessions.get(player.getUniqueId());
            if (session != null) {
                session.standardBar.removeAll();
                session.premiumBar.removeAll();
            }
            updateVisibility(player, false);
        }
        sessions.clear();
    }

    private void giveStandardReward(Player player) {
        double moneyAmount = config.getConfig().getDouble("settings.afk_zone.standard.reward_money");
        if (Main.getEconomy() != null) {
            Main.getEconomy().depositPlayer(player, moneyAmount);
        }
        player.sendMessage(config.getMessage("standard_reward_success").replace("{MONEY}", String.valueOf(moneyAmount)));
    }

    private void givePremiumReward(Player player) {
        if (ThreadLocalRandom.current().nextDouble(100.0) <= getPremiumChance(player)) {
            String cmd = config.getConfig().getString("settings.afk_zone.premium.reward_command").replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            player.sendMessage(config.getMessage("premium_reward_success"));
        } else {
            player.sendMessage(config.getMessage("premium_reward_fail"));
        }
    }

    private double getPremiumChance(Player player) {
        if (config.getConfig().contains("settings.afk_zone.premium.chances")) {
            for (String key : config.getConfig().getConfigurationSection("settings.afk_zone.premium.chances").getKeys(false)) {
                if (key.equalsIgnoreCase("default") || player.hasPermission("core.afk." + key)) {
                    return config.getConfig().getDouble("settings.afk_zone.premium.chances." + key);
                }
            }
        }
        return 0.0;
    }

    private BarColor parseBarColor(String color) {
        if (color == null) return BarColor.WHITE;
        try { return BarColor.valueOf(color.toUpperCase()); } catch (IllegalArgumentException e) { return BarColor.WHITE; }
    }

    private BarStyle parseBarOverlay(String style) {
        if (style == null) return BarStyle.SOLID;
        try { return BarStyle.valueOf(style.toUpperCase()); } catch (IllegalArgumentException e) { return BarStyle.SOLID; }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private static class AfkSession {
        int standardProgress = 0;
        int premiumProgress = 0;
        final BossBar standardBar;
        final BossBar premiumBar;

        AfkSession(BossBar standardBar, BossBar premiumBar) {
            this.standardBar = standardBar;
            this.premiumBar = premiumBar;
        }
    }
}