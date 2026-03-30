package dev.arab.TOOLS.DRAGON;

import dev.arab.Main;
import dev.arab.TOOLS.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class DragonBossManager {
    private final Main plugin;
    private final ConfigDragonBoss config;

    private EnderDragon activeDragon;
    private ArmorStand hologram;
    private BossBar bossBar;

    private boolean isCountingDown = false;
    private int countdownSeconds = 0;
    private int animationTick = 0;

    // Mapa przechowująca obrażenia: Gracz -> Ilość zadanych HP
    private final Map<Player, Double> damageMap = new HashMap<>();

    public DragonBossManager(Main plugin, ConfigDragonBoss config) {
        this.plugin = plugin;
        this.config = config;
        startScheduler();
    }

    private void startScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                LocalTime now = LocalTime.now();
                String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));

                // Sprawdzamy czy to czas na odliczanie (1 minuta przed spawnem)
                if (!isCountingDown && activeDragon == null) {
                    for (String timeStr : config.getConfig().getStringList("settings.spawn_times")) {
                        LocalTime spawnTime = LocalTime.parse(timeStr);
                        if (now.getHour() == spawnTime.minusMinutes(1).getHour() && now.getMinute() == spawnTime.minusMinutes(1).getMinute() && now.getSecond() == 0) {
                            startCountdown();
                            break;
                        }
                    }
                }

                // Odliczanie (jeśli aktywne)
                if (isCountingDown) {
                    countdownSeconds--;
                    if (bossBar != null) {
                        bossBar.setTitle(config.getMessage("bossbar_countdown").replace("{TIME}", String.valueOf(countdownSeconds)));
                    }

                    if (countdownSeconds <= 0) {
                        isCountingDown = false;
                        spawnDragon();
                    }
                }

                // Animacja BossBara i Hologramu, jeśli smok żyje
                if (activeDragon != null && activeDragon.isValid()) {
                    updateHologram();
                    if (animationTick++ % 60 == 0) { // Co 3 sekundy (60 ticków)
                        updateBossBar();
                    }
                } else if (activeDragon != null) {
                    cleanup(); // Awaryjne czyszczenie, jeśli smok zniknął (np. wpisano /kill)
                }
            }
        }.runTaskTimer(plugin, 20L, 1L); // Szybki timer do hologramu (1 tick), warunki czasowe oparte na 20 tickach
    }

    public void startCountdown() {
        if (isCountingDown || activeDragon != null) return; // To zabezpiecza przed podwójnym odpaleniem

        isCountingDown = true;
        countdownSeconds = 60;

        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(config.getMessage("bossbar_countdown").replace("{TIME}", "60"), BarColor.PURPLE, BarStyle.SOLID);
        }
        bossBar.setProgress(1.0);

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
        }
    }

    private void spawnDragon() {
        damageMap.clear();
        String worldName = config.getConfig().getString("locations.spawn.world", "spawn_the_end");
        World world = Bukkit.getWorld(worldName);

        // 1. Sprawdzanie czy świat w ogóle istnieje
        if (world == null) {
            plugin.getLogger().severe("============================================");
            plugin.getLogger().severe("BLAD: Swiat o nazwie '" + worldName + "' nie istnieje!");
            plugin.getLogger().severe("Smok nie moze sie zrespic. Zmien to w config-dragonboss.yml");
            plugin.getLogger().severe("============================================");
            Bukkit.broadcastMessage(ColorUtil.fixColor("&cWystapil blad spawnowania smoka (zly swiat w configu)!"));
            cleanup();
            return;
        }

        // 2. Sprawdzanie poziomu trudności (Peaceful natychmiast usuwa bossy)
        if (world.getDifficulty() == org.bukkit.Difficulty.PEACEFUL) {
            plugin.getLogger().warning("UWAGA: Swiat '" + worldName + "' ma poziom trudnosci PEACEFUL (Pokojowy). Boss moze od razu zniknac!");
        }

        double x = config.getConfig().getDouble("locations.spawn.x");
        double y = config.getConfig().getDouble("locations.spawn.y");
        double z = config.getConfig().getDouble("locations.spawn.z");
        Location loc = new Location(world, x, y, z);

        // 3. Spawnowanie smoka
        activeDragon = (EnderDragon) world.spawnEntity(loc, EntityType.ENDER_DRAGON);

        // Sprawdzanie czy plugin do zabezpieczeń lub WorldGuard go nie usunął
        if (activeDragon == null || !activeDragon.isValid()) {
            plugin.getLogger().severe("BLAD: Zrespiono smoka, ale serwer od razu go usunal! Sprawdz flagi WorldGuard (mob-spawning deny) lub inne pluginy usuwajace moby.");
            cleanup();
            return;
        }

        // 4. Wymuszamy zachowanie smoka, żeby krążył nad miejscem respu i nie uciekał w kosmos
        activeDragon.setPhase(EnderDragon.Phase.CIRCLING);

        // Ustawianie HP
        double maxHp = config.getConfig().getDouble("settings.dragon_max_health", 500.0);
        if (activeDragon.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            activeDragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHp);
            activeDragon.setHealth(maxHp);
        }

        // Spawnowanie Hologramu
        hologram = (ArmorStand) world.spawnEntity(loc.clone().add(0, 3, 0), EntityType.ARMOR_STAND);
        if (hologram != null) {
            hologram.setVisible(false);
            hologram.setMarker(true);
            hologram.setCustomNameVisible(true);
            hologram.setGravity(false);
            updateHologram();
        }

        // Broadcast Title
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(config.getMessage("title_spawn_main"), config.getMessage("title_spawn_sub"), 10, 70, 20);
            if (bossBar != null) bossBar.addPlayer(p);
        }

        if (bossBar != null) bossBar.setTitle(config.getMessage("bossbar_waiting"));
        animationTick = 0;
    }

    private void updateHologram() {
        if (activeDragon != null && hologram != null && activeDragon.isValid()) {
            hologram.teleport(activeDragon.getLocation().add(0, 4.5, 0));
            String hpFormat = String.format("%.1f", activeDragon.getHealth());
            hologram.setCustomName(config.getMessage("hologram_format").replace("{HP}", hpFormat));
        }
    }

    private void updateBossBar() {
        if (bossBar == null) return;

        if (damageMap.isEmpty()) {
            bossBar.setTitle(config.getMessage("bossbar_waiting"));
            return;
        }

        // Sortowanie graczy po ilości zadanych obrażeń
        List<Map.Entry<Player, Double>> sorted = damageMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());

        // Faza animacji (0 = TOP 1, 1 = TOP 2, 2 = TOP 3)
        int phase = (animationTick / 60) % 3;

        // Jeśli nie ma np. 3 graczy, a jest faza 3, wracamy do 0
        if (phase >= sorted.size()) {
            phase = 0;
        }

        Map.Entry<Player, Double> entry = sorted.get(phase);
        String format = config.getMessage("bossbar_top_format")
                .replace("{POSITION}", String.valueOf(phase + 1))
                .replace("{PLAYER}", entry.getKey().getName())
                .replace("{DMG}", String.format("%.1f", entry.getValue()));

        bossBar.setTitle(format);

        // Aktualizacja paska postępu (ile hp zostało smokowi)
        double maxHp = activeDragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, activeDragon.getHealth() / maxHp)));
    }

    public void addDamage(Player player, double damage) {
        damageMap.put(player, damageMap.getOrDefault(player, 0.0) + damage);
    }

    public void handleDragonDeath() {
        if (damageMap.isEmpty()) {
            Bukkit.broadcastMessage(config.getMessage("dragon_died_no_attackers"));
        } else {
            // Szukamy gracza z największą ilością obrażeń
            Player topDamager = damageMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (topDamager != null) {
                Bukkit.broadcastMessage(config.getMessage("dragon_died_winner")
                        .replace("{PLAYER}", topDamager.getName())
                        .replace("{DMG}", String.format("%.1f", damageMap.get(topDamager))));

                giveReward(topDamager);
            }
        }
        cleanup();
    }

    private void giveReward(Player player) {
        ConfigurationSection rewardsSection = config.getConfig().getConfigurationSection("rewards");
        if (rewardsSection == null) return;

        double random = ThreadLocalRandom.current().nextDouble(100.0);
        double currentChance = 0.0;

        for (String key : rewardsSection.getKeys(false)) {
            double chance = config.getConfig().getDouble("rewards." + key + ".chance");
            currentChance += chance;

            if (random <= currentChance) {
                String cmd = config.getConfig().getString("rewards." + key + ".command");
                if (cmd != null) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{PLAYER}", player.getName()));
                }
                break;
            }
        }
    }

    public void cleanup() {
        if (activeDragon != null && activeDragon.isValid()) activeDragon.remove();
        if (hologram != null && hologram.isValid()) hologram.remove();
        if (bossBar != null) bossBar.removeAll();
        activeDragon = null;
        hologram = null;
        isCountingDown = false;
        damageMap.clear();
    }

    public EnderDragon getActiveDragon() {
        return activeDragon;
    }

    public BossBar getBossBar() {
        return bossBar;
    }
}