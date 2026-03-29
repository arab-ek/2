package dev.arab.EVENTOWKICORE.eventowki;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class HydroKlatka extends EventItem {
    static final Map<String, HydroKlatka.HydroKlatkaInstance> activeCages = new ConcurrentHashMap<>();
    static final Map<UUID, String> playerActiveCages = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> recentCageEscapes = new ConcurrentHashMap<>();
    private static final Set<UUID> pendingShooters = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<Integer, HydroKlatka.SphereOffsets> sphereCache = new ConcurrentHashMap<>();
    int radius;
    int duration;
    Material shellMat;
    Material fillMat;
    private Set<String> iBlockedWorlds = new HashSet<>();
    private Set<String> iBlockedRegions = new HashSet<>();
    private boolean cutAtAllRegions;
    private String victimTitle;
    private String victimSub;
    private String blockedMsg;

    static long pack(int x, int y, int z) {
        return (long)(x & 67108863) << 38 | (long)(y & 4095) << 26 | (long)(z & 67108863);
    }

    static int unpackX(long packed) {
        return (int)(packed >> 38);
    }

    static int unpackY(long packed) {
        return (int)(packed >> 26) & 4095;
    }

    static int unpackZ(long packed) {
        return (int)(packed & 67108863L);
    }

    static HydroKlatka.SphereOffsets getSphereOffsets(int r) {
        return sphereCache.computeIfAbsent(r, HydroKlatka.SphereOffsets::new);
    }

    static String locToString(Location loc) {
        if (loc == null) {
            return "";
        } else {
            return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
        }
    }

    public HydroKlatka(Main plugin) {
        super(plugin, "hydro_klatka");
    }

    public boolean isBlockedByCage() {
        return true;
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.radius = this.plugin.getConfig().getInt("meta.hydro_klatka.radius", 10);
        this.duration = this.plugin.getConfig().getInt("meta.hydro_klatka.duration", 15);
        this.shellMat = Material.matchMaterial(this.plugin.getConfig().getString("meta.hydro_klatka.shell_material", "BLUE_GLAZED_TERRACOTTA"));
        if (this.shellMat == null) {
            this.shellMat = Material.BLUE_GLAZED_TERRACOTTA;
        }

        this.fillMat = Material.matchMaterial(this.plugin.getConfig().getString("meta.hydro_klatka.fill_material", "LIGHT_BLUE_CONCRETE"));
        if (this.fillMat == null) {
            this.fillMat = Material.LIGHT_BLUE_CONCRETE;
        }

        this.iBlockedWorlds = new HashSet<>(this.plugin.getConfig().getStringList("meta.hydro_klatka.blocked_worlds"));
        this.iBlockedRegions = new HashSet<>(this.plugin.getConfig().getStringList("meta.hydro_klatka.blocked_regions"));
        this.cutAtAllRegions = this.plugin.getConfig().getBoolean("meta.hydro_klatka.cut_at_all_regions", false);
        this.victimTitle = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.hydro_klatka.victim_title", "&c&lZŁAPANY"));
        this.victimSub = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.hydro_klatka.victim_subtitle", "&7Zostałeś uwięziony w Hydro Klatce!"));
        this.blockedMsg = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.hydro_klatka.blocked_message", "&cMasz już aktywną klatkę!"));
    }

    public void onTeleport(PlayerTeleportEvent event, Player player) {
        if (event.getCause() == TeleportCause.ENDER_PEARL && isInsideOrRecentlyInsideCage(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatUtils.color("&cKlatka blokuje teleportację!"));
        }

    }

    public void onMove(PlayerMoveEvent event, Player player) {
        if (!activeCages.isEmpty() && (player.isGliding() || player.isFlying()) && isInsideOrRecentlyInsideCage(player)) {
            if (player.isGliding()) {
                player.setGliding(false);
            }

            if (player.isFlying()) {
                player.setFlying(false);
            }
        }

    }

    public void onToggleFlight(PlayerToggleFlightEvent event, Player player) {
        if (isInsideOrRecentlyInsideCage(player)) {
            event.setCancelled(true);
            player.setFlying(false);
        }

    }

    private void removePearls(Player player) {
        if (player != null) {
            for (Entity entity : player.getWorld().getEntitiesByClass(EnderPearl.class)) {
                EnderPearl pearl = (EnderPearl) entity;
                if (pearl.getShooter() != null && pearl.getShooter().equals(player)) {
                    pearl.remove();
                }
            }
        }
    }

    boolean isLocBlocked(World world, int x, int y, int z, Set<String> centerRegions, Location reusableLoc) {
        if (!this.iBlockedWorlds.isEmpty() && this.iBlockedWorlds.contains(world.getName())) {
            return true;
        } else if (this.iBlockedRegions.isEmpty() && !this.cutAtAllRegions) {
            return false;
        } else {
            reusableLoc.setX((double)x + 0.5D);
            reusableLoc.setY((double)y + 0.5D);
            reusableLoc.setZ((double)z + 0.5D);
            Set<String> locRegions = this.plugin.getItemListener().getWorldGuardHook().getRegionIdsAtLocation(reusableLoc);
            if (!this.iBlockedRegions.isEmpty()) {
                for (String blocked : this.iBlockedRegions) {
                    if (locRegions.contains(blocked)) {
                        return true;
                    }
                }
            }

            return this.cutAtAllRegions && !locRegions.isEmpty() && !centerRegions.containsAll(locRegions);
        }
    }

    public static boolean isInsideCage(Location loc) {
        if (activeCages.isEmpty()) {
            return false;
        }
        for (HydroKlatkaInstance instance : activeCages.values()) {
            if (instance.isInside(loc)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInsideOrRecentlyInsideCage(Player player) {
        UUID uuid = player.getUniqueId();
        if (isInsideCage(player.getLocation())) {
            recentCageEscapes.put(uuid, System.currentTimeMillis());
            return true;
        } else {
            Long escapeTime = recentCageEscapes.get(uuid);
            if (escapeTime != null) {
                if (System.currentTimeMillis() - escapeTime < 1000L) {
                    return true;
                }

                recentCageEscapes.remove(uuid);
            }

            return false;
        }
    }

    public static void cleanupAll() {
        for (HydroKlatkaInstance instance : activeCages.values()) {
            try {
                instance.restoreBlocks();
            } catch (Exception ignored) {
            }
        }
        activeCages.clear();
        playerActiveCages.clear();
    }

    public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getAction().name().contains("RIGHT")) {
            event.setCancelled(true);
            this.handleHydroKlatka(player, item);
        }
    }

    private void handleHydroKlatka(Player player, ItemStack item) {
        if (!this.isBlocked(player) && !this.checkCooldown(player, item)) {
            pendingShooters.add(player.getUniqueId());
            this.removePearls(player);
            Fireball fireball = player.launchProjectile(Fireball.class);
            fireball.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
            fireball.setYield(0.0F);
            fireball.setIsIncendiary(false);
            this.applyUse(player, null, item, this.id);
        }
    }

    public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
        if (shooter != null) {
            pendingShooters.remove(shooter.getUniqueId());
            Location hitLoc = event.getHitEntity() != null ? event.getHitEntity().getLocation() : (event.getHitBlock() != null ? event.getHitBlock().getLocation() : null);
            if (hitLoc != null) {
                this.spawnCage(hitLoc, shooter);
            }
        }
    }

    private void spawnCage(Location rawCenter, Player shooter) {
        if (shooter != null) {
            Location center = rawCenter.getBlock().getLocation().add(0.5D, 0.5D, 0.5D);
            if (playerActiveCages.containsKey(shooter.getUniqueId())) {
                shooter.sendMessage(this.blockedMsg);
            } else if (!this.isBlocked(shooter)) {
                HydroKlatka.HydroKlatkaInstance instance = new HydroKlatka.HydroKlatkaInstance(center, shooter.getUniqueId());
                String locKey = locToString(center);
                activeCages.put(locKey, instance);
                playerActiveCages.put(shooter.getUniqueId(), locKey);

                for (Entity entity : center.getWorld().getNearbyEntities(center, this.radius, this.radius, this.radius)) {
                    if (entity instanceof Player) {
                        Player p = (Player)entity;
                        if (p != shooter) {
                            p.sendTitle(this.victimTitle, this.victimSub, 10, 40, 10);
                            this.removePearls(p);
                        }
                    }
                }

                instance.startSpawnAnimation(this.duration);
            }
        }
    }

    public void onBlockBreak(BlockBreakEvent event, Player player) {
        Block block = event.getBlock();
        HydroKlatkaInstance foundInstance = null;

        for (HydroKlatkaInstance instance : activeCages.values()) {
            if (instance.isCageBlock(block.getLocation())) {
                foundInstance = instance;
                break;
            }
        }

        if (foundInstance == null) return;

        if (!foundInstance.isShell(block.getLocation()) && !foundInstance.isGlass(block.getLocation())) {
            event.setDropItems(false);
        } else {
            event.setCancelled(true);
            player.sendTitle("", ChatUtils.color("&cNie mozesz tego zniszczyc!"), 0, 20, 0);
        }
    }

    private static class SphereOffsets {
        final List<HydroKlatka.RelativeBlock> shell;
        final List<HydroKlatka.RelativeBlock> interior;
        final int totalBlocks;

        SphereOffsets(int radius) {
            List<HydroKlatka.RelativeBlock> s = new ArrayList<>();
            List<HydroKlatka.RelativeBlock> i = new ArrayList<>();
            int r2 = radius * radius;
            int innerR2 = (radius - 1) * (radius - 1);

            for(int x = -radius; x <= radius; ++x) {
                int x2 = x * x;

                for(int y = -radius; y <= radius; ++y) {
                    int y2 = y * y;
                    if (x2 + y2 <= r2) {
                        for(int z = -radius; z <= radius; ++z) {
                            int dist2 = x2 + y2 + z * z;
                            if (dist2 <= r2) {
                                HydroKlatka.RelativeBlock rb = new HydroKlatka.RelativeBlock(x, y, z);
                                if (dist2 > innerR2) {
                                    s.add(rb);
                                } else {
                                    i.add(rb);
                                }
                            }
                        }
                    }
                }
            }

            this.shell = Collections.unmodifiableList(s);
            this.interior = Collections.unmodifiableList(i);
            this.totalBlocks = this.shell.size() + this.interior.size();
        }
    }

    private class HydroKlatkaInstance {
        final Location center;
        final Set<Long> shellLocations;
        final Set<Long> interiorLocations;
        final Set<Long> glassLocations;
        final Map<Long, BlockData> originalBlocks;
        private final Set<String> centerRegions;
        final UUID shooterUUID;
        int timeLeft;
        BossBar bossBar;
        private final Map<Long, Boolean> blockedCache;
        private final Location reusableLoc;

        public HydroKlatkaInstance(Location center, UUID shooterUUID) {
            this.center = center;
            this.shooterUUID = shooterUUID;
            this.centerRegions = HydroKlatka.this.plugin.getItemListener().getWorldGuardHook().getRegionIdsAtLocation(center);
            HydroKlatka.SphereOffsets offsets = HydroKlatka.getSphereOffsets(HydroKlatka.this.radius);
            int cap = (int)((float)offsets.totalBlocks / 0.75F) + 1;
            this.shellLocations = new HashSet<>(offsets.shell.size() * 2);
            this.interiorLocations = new HashSet<>(offsets.interior.size() * 2);
            this.glassLocations = new HashSet<>(offsets.totalBlocks / 4);
            this.originalBlocks = new HashMap<>(cap);
            this.blockedCache = new HashMap<>(cap / 2);
            this.reusableLoc = new Location(center.getWorld(), 0.0D, 0.0D, 0.0D);
            this.calculateSphere(offsets);
        }

        public boolean isInside(Location loc) {
            if (!loc.getWorld().equals(this.center.getWorld())) {
                return false;
            } else {
                return loc.distanceSquared(this.center) <= (double)(HydroKlatka.this.radius * HydroKlatka.this.radius);
            }
        }

        public boolean isCageBlock(Location loc) {
            return this.originalBlocks.containsKey(HydroKlatka.pack(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }

        private boolean cachedIsLocBlocked(int x, int y, int z) {
            long p = HydroKlatka.pack(x, y, z);
            Boolean cached = this.blockedCache.get(p);
            if (cached != null) {
                return cached;
            } else {
                boolean blocked = HydroKlatka.this.isLocBlocked(this.center.getWorld(), x, y, z, this.centerRegions, this.reusableLoc);
                this.blockedCache.put(p, blocked);
                return blocked;
            }
        }

        private void calculateSphere(HydroKlatka.SphereOffsets offsets) {
            int cx = this.center.getBlockX();
            int cy = this.center.getBlockY();
            int cz = this.center.getBlockZ();

            for (HydroKlatka.RelativeBlock rb : offsets.interior) {
                int bx = cx + rb.x;
                int by = cy + rb.y;
                int bz = cz + rb.z;
                if (!this.cachedIsLocBlocked(bx, by, bz)) {
                    long p = HydroKlatka.pack(bx, by, bz);
                    this.interiorLocations.add(p);
                    if (this.touchesBlockedBoundary(bx, by, bz) && by > cy) {
                        this.glassLocations.add(p);
                    }
                    this.recordOriginal(p, bx, by, bz);
                }
            }

            for (HydroKlatka.RelativeBlock rb : offsets.shell) {
                int bx = cx + rb.x;
                int by = cy + rb.y;
                int bz = cz + rb.z;
                if (!this.cachedIsLocBlocked(bx, by, bz)) {
                    long p = HydroKlatka.pack(bx, by, bz);
                    this.shellLocations.add(p);
                    if (this.touchesBlockedBoundary(bx, by, bz)) {
                        this.glassLocations.add(p);
                    }
                    this.recordOriginal(p, bx, by, bz);
                }
            }

            this.blockedCache.clear();
        }

        private void recordOriginal(long p, int bx, int by, int bz) {
            Block block = this.center.getWorld().getBlockAt(bx, by, bz);
            Material mat = block.getType();
            if (!this.shouldPreserveBlock(mat)) {
                BlockData originalData = null;
                for (HydroKlatkaInstance existingInstance : HydroKlatka.activeCages.values()) {
                    if (existingInstance != this && existingInstance.originalBlocks.containsKey(p)) {
                        originalData = existingInstance.originalBlocks.get(p);
                        break;
                    }
                }

                if (originalData == null) {
                    originalData = block.getBlockData();
                }

                this.originalBlocks.put(p, originalData);
            }

        }

        private boolean touchesBlockedBoundary(int bx, int by, int bz) {
            if (this.cachedIsLocBlocked(bx + 1, by, bz)) {
                return true;
            } else if (this.cachedIsLocBlocked(bx - 1, by, bz)) {
                return true;
            } else if (this.cachedIsLocBlocked(bx, by, bz + 1)) {
                return true;
            } else if (this.cachedIsLocBlocked(bx, by, bz - 1)) {
                return true;
            } else {
                return this.cachedIsLocBlocked(bx, by + 1, bz);
            }
        }

        boolean shouldPreserveBlock(Material mat) {
            return mat == Material.BEDROCK || mat.name().contains("PRESSURE_PLATE") || mat.name().contains("DOOR");
        }

        boolean shouldHideTemporarily(Material mat) {
            return mat.name().endsWith("_BUTTON") || mat == Material.LEVER || mat == Material.TRIPWIRE_HOOK;
        }

        public boolean isShell(Location loc) {
            return this.shellLocations.contains(HydroKlatka.pack(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }

        public boolean isGlass(Location loc) {
            return this.glassLocations.contains(HydroKlatka.pack(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }

        public void startSpawnAnimation(int dur) {
            this.timeLeft = dur;
            this.bossBar = Bukkit.createBossBar(ChatUtils.color("&b&lHydro Klatka"), BarColor.BLUE, BarStyle.SOLID, new BarFlag[0]);
            final Location centerPos = this.center.clone().add(0.5D, 0.5D, 0.5D);
            final World world = this.center.getWorld();
            final double rSq = ((double)HydroKlatka.this.radius + 0.5D) * ((double)HydroKlatka.this.radius + 0.5D);

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(world) && p.getLocation().distanceSquared(this.center) <= (double)((HydroKlatka.this.radius + 5) * (HydroKlatka.this.radius + 5))) {
                    this.bossBar.addPlayer(p);
                }
            }

            (new BukkitRunnable() {
                int currentY = HydroKlatka.this.radius;
                int tickCount = 0;

                public void run() {
                    try {
                        if (this.currentY < -HydroKlatka.this.radius) {
                            this.cancel();
                            HydroKlatkaInstance.this.startCountdown();
                            return;
                        }

                        double innerLimitSq = (double)((HydroKlatka.this.radius - 1) * (HydroKlatka.this.radius - 1));

                        for (Entity entity : world.getNearbyEntities(centerPos, HydroKlatka.this.radius + 1, HydroKlatka.this.radius + 1, HydroKlatka.this.radius + 1)) {
                            if (entity instanceof Player) {
                                Player px = (Player)entity;
                                if (!px.getUniqueId().equals(HydroKlatkaInstance.this.shooterUUID) && px.getGameMode() != GameMode.SPECTATOR) {
                                    double distSq = px.getLocation().distanceSquared(centerPos);
                                    if (distSq <= rSq && distSq > innerLimitSq) {
                                        Vector toCenter = centerPos.toVector().subtract(px.getLocation().toVector()).normalize();
                                        px.setVelocity(toCenter.multiply(0.4D));
                                    }
                                }
                            }
                        }

                        if (this.tickCount % 5 == 0) {
                            world.playSound(HydroKlatkaInstance.this.center.clone().add(0.0D, this.currentY, 0.0D), Sound.BLOCK_WATER_AMBIENT, 1.0F, 1.0F);
                        }

                        int cx = HydroKlatkaInstance.this.center.getBlockX();
                        int cy = HydroKlatkaInstance.this.center.getBlockY();
                        int cz = HydroKlatkaInstance.this.center.getBlockZ();

                        for(int x = -HydroKlatka.this.radius; x <= HydroKlatka.this.radius; ++x) {
                            for(int z = -HydroKlatka.this.radius; z <= HydroKlatka.this.radius; ++z) {
                                int bx = cx + x;
                                int by = cy + this.currentY;
                                int bz = cz + z;
                                long p = HydroKlatka.pack(bx, by, bz);
                                if (HydroKlatkaInstance.this.originalBlocks.containsKey(p)) {
                                    Block block = world.getBlockAt(bx, by, bz);
                                    Material currentMat = block.getType();
                                    if (HydroKlatkaInstance.this.shellLocations.contains(p)) {
                                        if (HydroKlatkaInstance.this.shouldPreserveBlock(currentMat) || currentMat == HydroKlatka.this.shellMat) {
                                            continue;
                                        }

                                        if (HydroKlatkaInstance.this.shouldHideTemporarily(currentMat)) {
                                            if (currentMat != Material.AIR) {
                                                block.setType(Material.AIR, false);
                                            }
                                            continue;
                                        }

                                        block.setType(HydroKlatka.this.shellMat, false);
                                    } else if (HydroKlatkaInstance.this.interiorLocations.contains(p) && by <= cy) {
                                        if (HydroKlatkaInstance.this.shouldPreserveBlock(currentMat) || currentMat == HydroKlatka.this.fillMat) {
                                            continue;
                                        }

                                        block.setType(HydroKlatka.this.fillMat, false);
                                    } else if (HydroKlatkaInstance.this.glassLocations.contains(p)) {
                                        if (currentMat == Material.LIGHT_BLUE_STAINED_GLASS) {
                                            continue;
                                        }

                                        block.setType(Material.LIGHT_BLUE_STAINED_GLASS, false);
                                    } else if (HydroKlatkaInstance.this.interiorLocations.contains(p)) {
                                        if (HydroKlatkaInstance.this.shouldPreserveBlock(currentMat) || currentMat == HydroKlatka.this.fillMat || currentMat == Material.AIR) {
                                            continue;
                                        }

                                        block.setType(HydroKlatka.this.fillMat, false);
                                    }

                                    if (HydroKlatka.this.plugin.getRandom().nextInt(100) == 0) {
                                        world.spawnParticle(Particle.WATER_DROP, (double)bx + 0.5D, (double)by + 0.5D, (double)bz + 0.5D, 5, 0.5D, 0.5D, 0.5D, 0.05D);
                                    }
                                }
                            }
                        }

                        --this.currentY;
                        ++this.tickCount;
                    } catch (Exception var15) {
                        var15.printStackTrace();
                        this.cancel();
                        HydroKlatkaInstance.this.startDespawnAnimation();
                    }

                }
            }).runTaskTimer(HydroKlatka.this.plugin, 0L, 1L);
        }

        void startCountdown() {
            final int totalTicks = HydroKlatka.this.duration * 20;
            this.timeLeft = totalTicks;
            final Location centerPos = this.center.clone().add(0.5D, 0.5D, 0.5D);

            (new BukkitRunnable() {
                private double lastProgress = -1.0D;
                private long lastPlayerUpdate = 0L;

                public void run() {
                    try {
                        if (HydroKlatkaInstance.this.timeLeft <= 0) {
                            this.cancel();
                            HydroKlatkaInstance.this.startDespawnAnimation();
                            return;
                        }

                        double currentProgress = Math.max(0.0D, Math.min(1.0D, (double)HydroKlatkaInstance.this.timeLeft / (double)totalTicks));
                        if (Math.abs(currentProgress - this.lastProgress) > 0.01D) {
                            HydroKlatkaInstance.this.bossBar.setProgress(currentProgress);
                            this.lastProgress = currentProgress;
                        }

                        long now = System.currentTimeMillis();
                        if (now - this.lastPlayerUpdate > 1000L) {
                            this.lastPlayerUpdate = now;
                            Set<Player> currentPlayers = new HashSet<>();

                            for (Entity entity : centerPos.getWorld().getNearbyEntities(centerPos, HydroKlatka.this.radius + 5, HydroKlatka.this.radius + 5, HydroKlatka.this.radius + 5)) {
                                if (entity instanceof Player) {
                                    Player p = (Player)entity;
                                    if (p.getGameMode() != GameMode.SPECTATOR) {
                                        currentPlayers.add(p);
                                    }
                                }
                            }

                            List<Player> playersToRemove = new ArrayList<>();
                            for (Player px : HydroKlatkaInstance.this.bossBar.getPlayers()) {
                                if (!currentPlayers.contains(px)) {
                                    playersToRemove.add(px);
                                }
                            }
                            playersToRemove.forEach(HydroKlatkaInstance.this.bossBar::removePlayer);

                            for (Player px : currentPlayers) {
                                if (!HydroKlatkaInstance.this.bossBar.getPlayers().contains(px)) {
                                    HydroKlatkaInstance.this.bossBar.addPlayer(px);
                                }
                            }
                        }

                        HydroKlatkaInstance.this.timeLeft -= 5;
                    } catch (Exception var9) {
                        var9.printStackTrace();
                        this.cancel();
                        HydroKlatkaInstance.this.startDespawnAnimation();
                    }

                }
            }).runTaskTimer(HydroKlatka.this.plugin, 0L, 5L);
        }

        void startDespawnAnimation() {
            if (this.bossBar != null) {
                this.bossBar.removeAll();
            }

            final World world = this.center.getWorld();
            (new BukkitRunnable() {
                int currentY = -HydroKlatka.this.radius;
                int tickCount = 0;

                public void run() {
                    try {
                        if (this.currentY > HydroKlatka.this.radius) {
                            this.cancel();
                            HydroKlatka.activeCages.remove(HydroKlatka.locToString(HydroKlatkaInstance.this.center));
                            HydroKlatka.playerActiveCages.remove(HydroKlatkaInstance.this.shooterUUID);
                            return;
                        }

                        if (this.tickCount % 5 == 0) {
                            world.playSound(HydroKlatkaInstance.this.center.clone().add(0.0D, this.currentY, 0.0D), Sound.ENTITY_BOAT_PADDLE_WATER, 1.0F, 1.0F);
                        }

                        int cx = HydroKlatkaInstance.this.center.getBlockX();
                        int cy = HydroKlatkaInstance.this.center.getBlockY();
                        int cz = HydroKlatkaInstance.this.center.getBlockZ();

                        for(int x = -HydroKlatka.this.radius; x <= HydroKlatka.this.radius; ++x) {
                            for(int z = -HydroKlatka.this.radius; z <= HydroKlatka.this.radius; ++z) {
                                int bx = cx + x;
                                int by = cy + this.currentY;
                                int bz = cz + z;
                                long p = HydroKlatka.pack(bx, by, bz);
                                BlockData originalData = HydroKlatkaInstance.this.originalBlocks.get(p);
                                if (originalData != null) {
                                    Block block = world.getBlockAt(bx, by, bz);
                                    if (!block.getBlockData().equals(originalData)) {
                                        block.setBlockData(originalData, false);
                                        if (HydroKlatka.this.plugin.getRandom().nextInt(100) == 0) {
                                            world.spawnParticle(Particle.WATER_DROP, (double)bx + 0.5D, (double)by + 0.5D, (double)bz + 0.5D, 5, 0.5D, 0.5D, 0.5D, 0.05D);
                                        }
                                    }
                                }
                            }
                        }

                        ++this.currentY;
                        ++this.tickCount;
                    } catch (Exception var13) {
                        var13.printStackTrace();
                        this.cancel();
                    }

                }
            }).runTaskTimer(HydroKlatka.this.plugin, 0L, 1L);
        }

        public void restoreBlocks() {
            if (this.bossBar != null) {
                this.bossBar.removeAll();
            }

            World world = this.center.getWorld();
            for (Entry<Long, BlockData> entry : this.originalBlocks.entrySet()) {
                long p = entry.getKey();
                world.getBlockAt(HydroKlatka.unpackX(p), HydroKlatka.unpackY(p), HydroKlatka.unpackZ(p)).setBlockData(entry.getValue(), false);
            }
        }
    }

    private static class RelativeBlock {
        final int x;
        final int y;
        final int z;

        RelativeBlock(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}