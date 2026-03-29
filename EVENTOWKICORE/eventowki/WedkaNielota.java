/* Decompiler 62ms, total 265ms, lines 163 */
package dev.arab.EVENTOWKICORE.eventowki;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WedkaNielota extends EventItem {
    private final Map<UUID, BossBar> activeBars = new HashMap();
    private final Map<UUID, BukkitTask> activeTimers = new HashMap();
    private String barTitle;

    public WedkaNielota(Main plugin) {
        super(plugin, "wedka_nielota");
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.barTitle = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.wedka_nielota.bar_title", "&c&lWędka Nielota"));
    }

    public void onLaunch(ProjectileLaunchEvent event, Player player, ItemStack item) {
        Projectile var5 = event.getEntity();
        if (var5 instanceof FishHook) {
            FishHook hook = (FishHook)var5;
            if (this.checkCooldown(player, item)) {
                event.setCancelled(true);
                return;
            }

            hook.getPersistentDataContainer().set(this.itemKey, PersistentDataType.STRING, this.id);
        }

    }

    public void onProjectileHit(ProjectileHitEvent event, Player shooter) {
        Entity var4 = event.getHitEntity();
        if (var4 instanceof Player) {
            Player victim = (Player)var4;
            if (shooter != null) {
                if (this.isBlocked(shooter)) {
                    return;
                }

                ItemStack chest = victim.getInventory().getChestplate();
                if (chest == null) {
                    return;
                }

                FishHook hook = (FishHook)event.getEntity();
                if (!this.plugin.getItemListener().getVictimHooks().containsKey(victim.getUniqueId())) {
                    this.sendUseNotification(shooter, victim);
                    this.sendVictimNotification(victim, shooter);
                }

                this.startHookTimer(shooter, victim, hook);
                if (victim.isGliding()) {
                    victim.setGliding(false);
                }
            }
        }

    }

    private void startHookTimer(Player shooter, final Player victim, final FishHook hook) {
        final UUID victimId = victim.getUniqueId();
        this.cancelTimer(victimId);
        final BossBar bar = Bukkit.createBossBar(this.barTitle, BarColor.RED, BarStyle.SOLID, new BarFlag[0]);
        bar.addPlayer(shooter);
        bar.addPlayer(victim);
        this.activeBars.put(victimId, bar);
        this.plugin.getItemListener().getVictimHooks().put(victimId, hook);
        this.activeTimers.put(victimId, (new BukkitRunnable() {
            int ticks = 300;

            public void run() {
                if (hook.isValid() && !hook.isDead() && victim.isOnline() && this.ticks > 0) {
                    bar.setProgress(Math.max(0.0D, (double)this.ticks / 300.0D));
                    --this.ticks;
                } else {
                    WedkaNielota.this.cleanup(victimId, hook);
                    this.cancel();
                }
            }
        }).runTaskTimer(this.plugin, 0L, 1L));
    }

    private void cancelTimer(UUID victimId) {
        BukkitTask task = (BukkitTask)this.activeTimers.remove(victimId);
        if (task != null) {
            task.cancel();
        }

        BossBar bar = (BossBar)this.activeBars.remove(victimId);
        if (bar != null) {
            bar.removeAll();
        }

    }

    void cleanup(UUID victimId, FishHook hook) {
        this.cancelTimer(victimId);
        this.plugin.getItemListener().getVictimHooks().remove(victimId);
        if (hook != null && hook.isValid()) {
            hook.remove();
        }

    }

    public void onFish(PlayerFishEvent event, Player player, ItemStack item) {
        if (event.getState() == State.CAUGHT_ENTITY) {
            Entity var5 = event.getCaught();
            if (var5 instanceof Player) {
                Player victim = (Player)var5;
                if (this.plugin.getItemListener().getVictimHooks().containsKey(victim.getUniqueId())) {
                    FishHook hook = (FishHook)this.plugin.getItemListener().getVictimHooks().get(victim.getUniqueId());
                    if (hook != null && hook.equals(event.getHook())) {
                        event.setCancelled(true);
                        this.applyUseSilent(player, victim, item, this.id);
                        return;
                    }
                }
            }
        }

        if (event.getState() == State.REEL_IN || event.getState() == State.IN_GROUND || event.getState() == State.FAILED_ATTEMPT || event.getState() == State.CAUGHT_FISH) {
            FishHook hook = event.getHook();
            if (hook == null) {
                return;
            }

            this.plugin.getItemListener().getVictimHooks().entrySet().removeIf((entry) -> {
                FishHook h = (FishHook)entry.getValue();
                if (h != null && h.equals(hook)) {
                    this.cancelTimer((UUID)entry.getKey());
                    return true;
                } else {
                    return h == null || !h.isValid();
                }
            });
        }

    }
}