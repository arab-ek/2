package dev.arab.EVENTOWKICORE.eventowki;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class RozdzkaIluzjonisty extends EventItem {
    private static final Map<UUID, RozdzkaIluzjonisty.StoredEquipment> hiddenPlayers = new HashMap<>();
    int invisDur;
    private final NamespacedKey isCloneKey;
    private final NamespacedKey fangsKey;
    private final NamespacedKey fangsOwnerKey;

    public static boolean isPlayerHidden(UUID uuid) {
        return hiddenPlayers.containsKey(uuid);
    }

    public RozdzkaIluzjonisty(Main plugin) {
        super(plugin, "rozdzka_iluzjonisty");
        this.isCloneKey = new NamespacedKey(plugin, "is_clone");
        this.fangsKey = new NamespacedKey(plugin, "iluzjonista_fangs");
        this.fangsOwnerKey = new NamespacedKey(plugin, "fangs_owner");
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.invisDur = this.plugin.getConfig().getInt("meta.rozdzka_iluzjonisty.invis_duration", 4);
    }

    public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                this.handleRozdzkaPPM(player, item);
            }
        } else {
            event.setCancelled(true);
            this.handleRozdzkaLPM(player, item);
        }
    }

    private void handleRozdzkaLPM(Player player, ItemStack item) {
        if (!this.isBlocked(player) && !this.checkCooldown(player, item, this.id + ".lpm")) {
            Location loc = player.getLocation();
            Vector dir = loc.getDirection().setY(0).normalize();

            for(int offset = -1; offset <= 1; ++offset) {
                Vector sideDir = (new Vector(-dir.getZ(), 0.0D, dir.getX())).multiply((double)offset * 0.8D);

                for(int i = 1; i <= 6; ++i) {
                    loc.getWorld().spawn(loc.clone().add(dir.clone().multiply(i)).add(sideDir), EvokerFangs.class, (fangs) -> {
                        PersistentDataContainer pdc = fangs.getPersistentDataContainer();
                        pdc.set(this.fangsKey, PersistentDataType.BYTE, (byte)1);
                        pdc.set(this.fangsOwnerKey, PersistentDataType.STRING, player.getUniqueId().toString());
                    });
                }
            }

            this.applyUse(player, null, item, this.id + ".lpm", this.id + ".lpm");
        }
    }

    @SuppressWarnings("deprecation")
    private void handleRozdzkaPPM(final Player player, ItemStack item) {
        if (!this.isBlocked(player) && !this.checkCooldown(player, item, this.id + ".ppm")) {
            if (!hiddenPlayers.containsKey(player.getUniqueId())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, this.invisDur * 20, 0, false, false));
                player.setInvisible(true);
                RozdzkaIluzjonisty.StoredEquipment eq = new RozdzkaIluzjonisty.StoredEquipment(
                        player.getInventory().getArmorContents(),
                        player.getInventory().getItemInMainHand(),
                        player.getInventory().getHeldItemSlot(),
                        player.getInventory().getItemInOffHand()
                );
                hiddenPlayers.put(player.getUniqueId(), eq);
                player.updateInventory();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p != player) {
                        p.hidePlayer(this.plugin, player);
                    }
                }

                player.setInvulnerable(true);
                player.setCollidable(false);

                final ArmorStand clone = player.getWorld().spawn(player.getLocation(), ArmorStand.class, (as) -> {
                    as.getPersistentDataContainer().set(this.isCloneKey, PersistentDataType.BYTE, (byte)1);
                    as.setVisible(false);
                    as.setCustomName(ChatColor.WHITE + player.getName());
                    as.setCustomNameVisible(true);
                    as.getEquipment().setArmorContents(eq.armor);
                    as.getEquipment().setItemInMainHand(eq.mainHand);
                    as.getEquipment().setItemInOffHand(eq.offHand);
                    as.setGravity(true);
                    as.setInvulnerable(true);
                    as.setBasePlate(false);
                    as.setArms(true);
                    as.setGlowing(false);

                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        as.addEquipmentLock(slot, LockType.REMOVING_OR_CHANGING);
                    }
                });

                final Vector moveDir = player.getLocation().getDirection().setY(0).normalize().multiply(0.15D);
                (new BukkitRunnable() {
                    int ticks = 0;

                    public void run() {
                        if (this.ticks++ < RozdzkaIluzjonisty.this.invisDur * 20 && !clone.isDead()) {
                            clone.teleport(clone.getLocation().add(moveDir));
                        } else {
                            clone.remove();
                            RozdzkaIluzjonisty.this.restoreEquipment(player);
                            this.cancel();
                        }
                    }
                }).runTaskTimer(this.plugin, 0L, 1L);

                this.applyUse(player, null, item, this.id + ".ppm");
            }
        }
    }

    void restoreEquipment(Player player) {
        hiddenPlayers.remove(player.getUniqueId());
        player.updateInventory();
        player.setInvisible(false);
        player.setInvulnerable(false);
        player.setCollidable(true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p != player) {
                p.showPlayer(this.plugin, player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.restoreEquipment(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        for (UUID hiddenId : hiddenPlayers.keySet()) {
            Player hidden = Bukkit.getPlayer(hiddenId);
            if (hidden != null) {
                joiner.hidePlayer(this.plugin, hidden);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && hiddenPlayers.containsKey(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent ebe = (EntityDamageByEntityEvent)event;
                Entity damager = ebe.getDamager();
                if (damager instanceof Projectile) {
                    Projectile proj = (Projectile)damager;
                    proj.remove();
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player && hiddenPlayers.containsKey(event.getTarget().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player && hiddenPlayers.containsKey(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFangsDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof EvokerFangs) {
            EvokerFangs fangs = (EvokerFangs)damager;
            PersistentDataContainer pdc = fangs.getPersistentDataContainer();
            if (pdc.has(this.fangsKey, PersistentDataType.BYTE)) {
                Entity victimEntity = event.getEntity();
                if (victimEntity instanceof Player) {
                    Player victim = (Player)victimEntity;
                    String ownerUUIDStr = pdc.get(this.fangsOwnerKey, PersistentDataType.STRING);
                    if (ownerUUIDStr != null && victim.getUniqueId().toString().equals(ownerUUIDStr)) {
                        event.setCancelled(true);
                    } else {
                        event.setDamage(0.01D);
                        double newHealth = Math.max(0.0D, victim.getHealth() - 8.0D);
                        victim.setHealth(newHealth);
                    }
                }
            }
        }
    }

    public static class StoredEquipment {
        public ItemStack[] armor;
        public ItemStack mainHand;
        public int mainHandSlot;
        public ItemStack offHand;

        public StoredEquipment(ItemStack[] armor, ItemStack mainHand, int mainHandSlot, ItemStack offHand) {
            this.armor = armor;
            this.mainHand = mainHand;
            this.mainHandSlot = mainHandSlot;
            this.offHand = offHand;
        }
    }
}