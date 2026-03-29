/* Decompiler 20ms, total 191ms, lines 98 */
package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class PiekielnyMiecz extends EventItem {
    int fireDur;
    double fireDmg;

    public PiekielnyMiecz(Main plugin) {
        super(plugin, "piekielny_miecz");
    }

    public void onGive(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.DAMAGE_ALL, 6, true);
            meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
            meta.addEnchant(Enchantment.DURABILITY, 3, true);
            this.applyHideFlags(meta);
            item.setItemMeta(meta);
        }

    }

    public boolean updateItem(ItemStack item, ItemMeta meta) {
        boolean changed = false;
        if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) < 6) {
            meta.addEnchant(Enchantment.DAMAGE_ALL, 6, true);
            changed = true;
        }

        if (meta.getEnchantLevel(Enchantment.FIRE_ASPECT) < 2) {
            meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
            changed = true;
        }

        if (meta.getEnchantLevel(Enchantment.DURABILITY) < 3) {
            meta.addEnchant(Enchantment.DURABILITY, 3, true);
            changed = true;
        }

        if (!meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
            this.applyHideFlags(meta);
            changed = true;
        }

        return changed;
    }

    public void reloadConfigCache() {
        super.reloadConfigCache();
        this.fireDur = this.plugin.getConfig().getInt("meta.piekielny_miecz.fire_duration", 5);
        this.fireDmg = this.plugin.getConfig().getDouble("meta.piekielny_miecz.fire_damage", 1.0D);
    }

    public void onDamageDamager(EntityDamageByEntityEvent event, Player damager, Player victim, ItemStack item) {
        if (!this.isBlocked(damager) && !this.checkCooldown(damager, item)) {
            if (event.getCause() == DamageCause.ENTITY_ATTACK && !victim.hasMetadata("hellfire_burning")) {
                victim.setMetadata("hellfire_burning", new FixedMetadataValue(this.plugin, true));
                this.applyUse(damager, victim, item, this.id);
                this.sendVictimNotification(victim, damager);
                this.applyHellFire(victim, damager);
            }

        }
    }

    private void applyHellFire(final Player victim, final Player damager) {
        (new BukkitRunnable() {
            int ticks;

            {
                this.ticks = PiekielnyMiecz.this.fireDur;
            }

            public void run() {
                if (this.ticks > 0 && !victim.isDead() && victim.isOnline()) {
                    victim.damage(PiekielnyMiecz.this.fireDmg, damager);
                    victim.setFireTicks(20);
                    --this.ticks;
                } else {
                    victim.removeMetadata("hellfire_burning", PiekielnyMiecz.this.plugin);
                    this.cancel();
                }
            }
        }).runTaskTimer(this.plugin, 0L, 20L);
    }
}