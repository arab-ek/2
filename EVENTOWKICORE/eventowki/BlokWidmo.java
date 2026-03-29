/* Decompiler 161ms, total 502ms, lines 321 */
package dev.arab.EVENTOWKICORE.eventowki;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class BlokWidmo extends EventItem {
    static final Map<Location, BlokWidmo.BlockInfo> activeBlocks = new ConcurrentHashMap();
    static final Map<UUID, Long> playerExpirations = new ConcurrentHashMap();
    static final Map<UUID, BossBar> activeBossBars = new ConcurrentHashMap();
    static final UUID HEALTH_REDUCTION_UUID = UUID.fromString("6a117d91-bc1e-450f-a789-982348567abc");
    private long durationMillis;
    private double radius;
    String victimTitleMsg;
    String victimSubMsg;
    private String barMsgPattern;

    public BlokWidmo(Main plugin) {
        super(plugin, "blok_widmo");
        this.loadBlocks();
        this.startTask();
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.durationMillis = (long)this.plugin.getConfig().getInt("meta.blok_widmo.duration", 90) * 1000L;
        this.radius = this.plugin.getConfig().getDouble("meta.blok_widmo.radius", 5.0D);
        this.victimTitleMsg = ChatUtils.color(this.plugin.getMessages().getString("items.blok_widmo.victim_title"));
        this.victimSubMsg = ChatUtils.color(this.plugin.getMessages().getString("items.blok_widmo.victim_subtitle"));
        this.barMsgPattern = this.plugin.getMessages().getString("items.blok_widmo.bossbar", "");
    }

    void saveBlocks() {
        YamlConfiguration config = new YamlConfiguration();
        int i = 0;

        for(Iterator var3 = activeBlocks.entrySet().iterator(); var3.hasNext(); ++i) {
            Entry<Location, BlokWidmo.BlockInfo> entry = (Entry)var3.next();
            config.set("blocks." + i + ".loc", entry.getKey());
            config.set("blocks." + i + ".expires", ((BlokWidmo.BlockInfo)entry.getValue()).expires);
            config.set("blocks." + i + ".owner", ((BlokWidmo.BlockInfo)entry.getValue()).owner.toString());
        }

        String data = config.saveToString();
        File file = new File(this.plugin.getDataFolder(), "blok_widmo_data.yml");
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                FileWriter writer = new FileWriter(file);

                try {
                    writer.write(data);
                } catch (Throwable var6) {
                    try {
                        writer.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }

                    throw var6;
                }

                writer.close();
            } catch (Exception var7) {
            }

        });
    }

    private void loadBlocks() {
        File file = new File(this.plugin.getDataFolder(), "blok_widmo_data.yml");
        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (config.contains("blocks")) {
                long now = System.currentTimeMillis();
                Iterator var5 = config.getConfigurationSection("blocks").getKeys(false).iterator();

                while(var5.hasNext()) {
                    String key = (String)var5.next();
                    Location loc = config.getLocation("blocks." + key + ".loc");
                    long expires = config.getLong("blocks." + key + ".expires");
                    String ownerStr = config.getString("blocks." + key + ".owner");
                    if (loc != null && ownerStr != null) {
                        if (expires > now) {
                            activeBlocks.put(loc, new BlokWidmo.BlockInfo(expires, UUID.fromString(ownerStr)));
                        } else {
                            loc.getBlock().setType(Material.AIR);
                        }
                    }
                }

                this.saveBlocks();
            }
        }
    }

    public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            event.setCancelled(true);
            Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
            if (!this.isBlocked(player, loc) && !this.checkCooldown(player, item)) {
                if (loc.getBlock().getType().isAir() || loc.getBlock().isReplaceable()) {
                    this.applyUse(player, (Player)null, item, this.id);
                    loc.getBlock().setType(this.getBaseMaterial() != null ? this.getBaseMaterial() : Material.STRUCTURE_BLOCK);
                    loc.getWorld().playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 0.5F);
                    loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc.clone().add(0.5D, 0.5D, 0.5D), 50, 0.5D, 0.5D, 0.5D, 0.1D);
                    activeBlocks.put(loc, new BlokWidmo.BlockInfo(System.currentTimeMillis() + this.durationMillis, player.getUniqueId()));
                    this.saveBlocks();
                    Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                        if (activeBlocks.containsKey(loc)) {
                            loc.getBlock().setType(Material.AIR);
                        }

                    }, 1L);
                }
            }
        }
    }

    private void startTask() {
        final double radiusSq = this.radius * this.radius;
        (new BukkitRunnable() {
            public void run() {
                long now = System.currentTimeMillis();
                boolean removed = false;
                Iterator it = BlokWidmo.activeBlocks.entrySet().iterator();

                while(it.hasNext()) {
                    Entry<Location, BlokWidmo.BlockInfo> entry = (Entry)it.next();
                    if (((BlokWidmo.BlockInfo)entry.getValue()).expires < now) {
                        ((Location)entry.getKey()).getBlock().setType(Material.AIR);
                        it.remove();
                        removed = true;
                    }
                }

                if (removed) {
                    BlokWidmo.this.saveBlocks();
                }

                Iterator expIt = BlokWidmo.activeBlocks.entrySet().iterator();

                Entry entryx;
                while(expIt.hasNext()) {
                    entryx = (Entry)expIt.next();
                    Location center = ((Location)entryx.getKey()).clone().add(0.5D, 0.5D, 0.5D);
                    Iterator var8 = center.getWorld().getPlayers().iterator();

                    while(var8.hasNext()) {
                        Player p = (Player)var8.next();
                        if (!p.getUniqueId().equals(((BlokWidmo.BlockInfo)entryx.getValue()).owner) && p.getGameMode() != GameMode.SPECTATOR && p.getLocation().distanceSquared(center) <= radiusSq) {
                            UUID uuid = p.getUniqueId();
                            long currentExp = (Long)BlokWidmo.playerExpirations.getOrDefault(uuid, 0L);
                            if (((BlokWidmo.BlockInfo)entryx.getValue()).expires > currentExp) {
                                BlokWidmo.playerExpirations.put(uuid, ((BlokWidmo.BlockInfo)entryx.getValue()).expires);
                            }
                        }
                    }
                }

                expIt = BlokWidmo.playerExpirations.entrySet().iterator();

                while(true) {
                    while(expIt.hasNext()) {
                        entryx = (Entry)expIt.next();
                        UUID uuidx = (UUID)entryx.getKey();
                        long expires = (Long)entryx.getValue();
                        Player px = Bukkit.getPlayer(uuidx);
                        AttributeInstance attr;
                        if (px != null && px.isOnline() && now < expires) {
                            attr = px.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            if (attr != null) {
                                boolean hasMod = false;
                                Iterator var22 = attr.getModifiers().iterator();

                                while(var22.hasNext()) {
                                    AttributeModifier modx = (AttributeModifier)var22.next();
                                    if (modx.getUniqueId().equals(BlokWidmo.HEALTH_REDUCTION_UUID)) {
                                        hasMod = true;
                                        break;
                                    }
                                }

                                if (!hasMod) {
                                    attr.addModifier(new AttributeModifier(BlokWidmo.HEALTH_REDUCTION_UUID, "blok_widmo_reduction", -20.0D, Operation.ADD_NUMBER));
                                    if (px.getHealth() > attr.getValue()) {
                                        px.setHealth(attr.getValue());
                                    }

                                    px.sendTitle(BlokWidmo.this.victimTitleMsg, BlokWidmo.this.victimSubMsg, 10, 40, 10);
                                }
                            }

                            long remaining = expires - now;
                            BossBar bar = (BossBar)BlokWidmo.activeBossBars.computeIfAbsent(uuidx, (k) -> {
                                BossBar b = Bukkit.createBossBar(BlokWidmo.this.formatBossBar(remaining), BarColor.PURPLE, BarStyle.SOLID, new BarFlag[0]);
                                b.addPlayer(px);
                                return b;
                            });
                            bar.setTitle(BlokWidmo.this.formatBossBar(remaining));
                            bar.setProgress(1.0D);
                        } else {
                            if (px != null) {
                                attr = px.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                                if (attr != null) {
                                    Iterator var12 = (new ArrayList(attr.getModifiers())).iterator();

                                    while(var12.hasNext()) {
                                        AttributeModifier mod = (AttributeModifier)var12.next();
                                        if (mod.getUniqueId().equals(BlokWidmo.HEALTH_REDUCTION_UUID)) {
                                            attr.removeModifier(mod);
                                        }
                                    }
                                }
                            }

                            BossBar barx = (BossBar)BlokWidmo.activeBossBars.remove(uuidx);
                            if (barx != null) {
                                barx.removeAll();
                            }

                            expIt.remove();
                        }
                    }

                    return;
                }
            }
        }).runTaskTimer(this.plugin, 10L, 10L);
    }

    String formatBossBar(long millisLeft) {
        long seconds = millisLeft / 1000L;
        String timeStr = String.format("%dm %ds", seconds / 60L, seconds % 60L);
        return ChatUtils.color(this.barMsgPattern.replace("%time%", timeStr));
    }

    public static void cleanupAll() {
        Iterator var0 = activeBossBars.values().iterator();

        while(var0.hasNext()) {
            BossBar bar = (BossBar)var0.next();
            bar.removeAll();
        }

        activeBossBars.clear();
        var0 = playerExpirations.keySet().iterator();

        while(true) {
            AttributeInstance attr;
            do {
                Player p;
                do {
                    if (!var0.hasNext()) {
                        playerExpirations.clear();
                        var0 = activeBlocks.keySet().iterator();

                        while(var0.hasNext()) {
                            Location loc = (Location)var0.next();
                            loc.getBlock().setType(Material.AIR);
                        }

                        activeBlocks.clear();
                        return;
                    }

                    UUID uuid = (UUID)var0.next();
                    p = Bukkit.getPlayer(uuid);
                } while(p == null);

                attr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            } while(attr == null);

            Iterator var4 = (new ArrayList(attr.getModifiers())).iterator();

            while(var4.hasNext()) {
                AttributeModifier mod = (AttributeModifier)var4.next();
                if (mod.getUniqueId().equals(HEALTH_REDUCTION_UUID)) {
                    attr.removeModifier(mod);
                }
            }
        }
    }

    private static class BlockInfo {
        final long expires;
        final UUID owner;

        BlockInfo(long expires, UUID owner) {
            this.expires = expires;
            this.owner = owner;
        }
    }
}