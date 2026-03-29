/* Decompiler 14ms, total 368ms, lines 42 */
package dev.arab.EVENTOWKICORE.eventowki;

import java.util.Iterator;
import dev.arab.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class KupaAnarchii extends EventItem {
    public KupaAnarchii(Main plugin) {
        super(plugin, "kupa_anarchii");
        this.startPassiveEffect();
    }

    private void startPassiveEffect() {
        (new BukkitRunnable() {
            public void run() {
                Iterator var1 = KupaAnarchii.this.plugin.getServer().getOnlinePlayers().iterator();

                while(true) {
                    Player p;
                    ItemStack main;
                    ItemStack off;
                    do {
                        if (!var1.hasNext()) {
                            return;
                        }

                        p = (Player)var1.next();
                        main = p.getInventory().getItemInMainHand();
                        off = p.getInventory().getItemInOffHand();
                    } while(!KupaAnarchii.this.isCustomItem(main) && !KupaAnarchii.this.isCustomItem(off));

                    p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, 1, false, false));
                }
            }
        }).runTaskTimer(this.plugin, 0L, 20L);
    }
}