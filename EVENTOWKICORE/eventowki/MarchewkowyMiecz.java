/* Decompiler 11ms, total 189ms, lines 39 */
package dev.arab.EVENTOWKICORE.eventowki;

import java.util.Set;
import java.util.UUID;
import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class MarchewkowyMiecz extends EventItem {
    private int freezeTicks;

    public MarchewkowyMiecz(Main plugin) {
        super(plugin, "marchewkowy_miecz");
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.freezeTicks = this.plugin.getConfig().getInt("meta.marchewkowy_miecz.freeze_seconds", 3) * 20;
    }

    public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
        if (!this.isBlocked(damager) && !this.checkCooldown(damager, item)) {
            victim.setFreezeTicks(this.freezeTicks);
            final UUID vid = victim.getUniqueId();
            final Set<UUID> frozen = this.plugin.getItemListener().getFrozenPlayers();
            frozen.add(vid);
            (new BukkitRunnable() {
                public void run() {
                    frozen.remove(vid);
                }
            }).runTaskLater(this.plugin, (long)this.freezeTicks);
            this.sendVictimNotification(victim, damager);
            this.applyUse(damager, victim, item, this.id);
        }
    }
}