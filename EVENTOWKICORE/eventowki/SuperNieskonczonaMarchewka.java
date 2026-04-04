package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.EVENTOWKICORE.utils.CooldownManager;
import dev.arab.TOOLS.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Zmieniono na extends EventItem (jeśli dalej będzie błąd, usuń słowo extends EventItem, lub zamień na to, co mają inne eventówki)
public class SuperNieskonczonaMarchewka extends EventItem implements Listener {
    private final Plugin plugin;
    private final CooldownManager cooldownManager;

    private final Map<UUID, Long> activeCrits = new ConcurrentHashMap<>();

    public SuperNieskonczonaMarchewka(Plugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getIdentifier() {
        return "supernieskonczonamarchewka";
    }

    @Override
    public ItemStack getItem() {
        FileConfiguration config = plugin.getConfig();
        String path = "meta.supernieskonczonamarchewka.";

        ItemStack item = new ItemStack(Material.GOLDEN_CARROT);
        ItemMeta meta = item.getItemMeta();

        String name = config.getString(path + "name", "&6&lSuper Nieskonczona Marchewka");
        // Używamy setDisplayName (przestarzałe w nowych API, ale w starych to standard)
        meta.setDisplayName(ColorUtil.fixColor(name));

        List<String> rawLore = config.getStringList(path + "lore");
        if (rawLore == null || rawLore.isEmpty()) {
            rawLore = Arrays.asList(
                    "&8» &7Pochodzenie: &cMagiczna Skrzynia",
                    "",
                    "&8» &7Zwieksza twoja skale do: &62.5",
                    "&8» &7Czas trwania: &630 sekund",
                    "&8» &7Dodatkowe efekty:",
                    "&8- &7Spowolnienie II",
                    "&8- &7Odpornosc II",
                    "&8- &7Zwiekszone obrazenia z kryta",
                    "",
                    "&8» &7Cooldown: &660 sekund"
            );
        }

        List<String> coloredLore = new ArrayList<>();
        for (String line : rawLore) {
            coloredLore.add(ColorUtil.fixColor(line));
        }

        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId(); // Poprawka dla CooldownManagera

        FileConfiguration config = plugin.getConfig();
        String path = "meta.supernieskonczonamarchewka.";

        int cooldown = config.getInt(path + "cooldown", 60);
        int durationSeconds = config.getInt(path + "duration_seconds", 30);
        double scale = config.getDouble(path + "scale_multiplier", 2.5);

        // Zmień isOnCooldown na metodę, której używają inne Twoje eventówki (np. hasCooldown)
        // Zakładam standardowe nazwy: hasCooldown i getCooldown (jeśli są inne - popraw to)
        if (cooldownManager.hasCooldown(uuid, getIdentifier())) {
            // Zmień getCooldown na to czego używa Twój system
            String time = String.valueOf(cooldownManager.getCooldown(uuid, getIdentifier()));
            player.sendMessage(ColorUtil.fixColor("&cMusisz poczekac &6" + time + "s &cprzed kolejnym uzyciem!"));
            return;
        }

        cooldownManager.setCooldown(uuid, getIdentifier(), cooldown);

        // Zmiana SLOWNESS na SLOW oraz RESISTANCE na DAMAGE_RESISTANCE dla kompatybilności
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, durationSeconds * 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, durationSeconds * 20, 2));

        // Bezpieczna zmiana skali przez refleksję (nie wywali błędu w IDE, zadziała na serwerach 1.20.5+)
        setPlayerScale(player, scale);

        activeCrits.put(uuid, System.currentTimeMillis() + (durationSeconds * 1000L));
        player.sendMessage(ColorUtil.fixColor("&aZjadles Super Nieskonczona Marchewke! Urosles do ogromnych rozmiarow!"));

        new BukkitRunnable() {
            @Override
            public void run() {
                activeCrits.remove(uuid);

                if (player.isOnline()) {
                    setPlayerScale(player, 1.0);
                    player.sendMessage(ColorUtil.fixColor("&cEfekt Super Nieskonczonej Marchewki minal. Wrociles do normalnych rozmiarow!"));
                }
            }
        }.runTaskLater(plugin, durationSeconds * 20L);
    }

    @EventHandler
    public void onCriticalHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (activeCrits.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() > activeCrits.get(player.getUniqueId())) {
                activeCrits.remove(player.getUniqueId());
                return;
            }

            boolean isCrit = false;
            try {
                isCrit = event.isCritical();
            } catch (NoSuchMethodError ignored) {
                if (player.getFallDistance() > 0.0F && !player.isOnGround() && !player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                    isCrit = true;
                }
            }

            if (isCrit) {
                double critMultiplier = plugin.getConfig().getDouble("meta.supernieskonczonamarchewka.crit_damage_multiplier", 1.5);
                event.setDamage(event.getDamage() * critMultiplier);
            }
        }
    }

    // Pomocnicza metoda ukrywająca atrybut skali przed starym kompilatorem Bukkita
    private void setPlayerScale(Player player, double scale) {
        try {
            Class<?> attributeClass = Class.forName("org.bukkit.attribute.Attribute");
            Object genericScale = Enum.valueOf((Class<Enum>) attributeClass, "GENERIC_SCALE");

            Method getAttributeMethod = player.getClass().getMethod("getAttribute", attributeClass);
            Object attributeInstance = getAttributeMethod.invoke(player, genericScale);

            if (attributeInstance != null) {
                Method setBaseValueMethod = attributeInstance.getClass().getMethod("setBaseValue", double.class);
                setBaseValueMethod.invoke(attributeInstance, scale);
            }
        } catch (Exception ignored) {
            // Serwer działa na wersji starszej niż 1.20.5 - ignorujemy zmianę skali
        }
    }
}