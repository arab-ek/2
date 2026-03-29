package dev.arab.EVENTOWKICORE.eventowki;

import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.EquipmentSlot;

public class WataCukrowa extends EventItem {
  private String noRepairMsg;

  public WataCukrowa(Main plugin) {
    super(plugin, "wata_cukrowa");
  }

  @Override
  public void reloadConfigCache() {
    super.reloadConfigCache();
    this.noRepairMsg = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.wata_cukrowa.no_repair_needed", "&cTwoja zbroja nie wymaga naprawy!"));
  }

  @Override
  public void onInteract(PlayerInteractEvent event, Player player, ItemStack item) {
    if (event.getAction().name().contains("RIGHT")) {
      event.setCancelled(true);
      // Przekazujemy również rękę, w której trzymany jest przedmiot
      this.handleWataCukrowa(player, item, event.getHand());
    }
  }

  private void handleWataCukrowa(Player player, ItemStack item, EquipmentSlot hand) {
    if (!this.isBlocked(player) && !this.checkCooldown(player, item)) {
      ItemStack[] armor = player.getInventory().getArmorContents();
      boolean repaired = false;

      for(int i = 0; i < armor.length; ++i) {
        ItemStack piece = armor[i];
        if (piece != null && piece.getType() != Material.AIR && piece.hasItemMeta()) {
          ItemMeta meta = piece.getItemMeta();
          if (meta instanceof Damageable d) {
            if (d.getDamage() > 0) {
              d.setDamage(0);
              piece.setItemMeta(d);
              repaired = true;
              break;
            }
          }
        }
      }

      if (repaired) {
        player.getInventory().setArmorContents(armor);
        this.applyUse(player, null, item, this.id);

        // Zabezpieczenie zniknięcia itemu z eq jeśli został zużyty do końca (ilość to 0)
        if (!this.infinite) {
          if (hand == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(item.getAmount() > 0 ? item : null);
          } else if (hand == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(item.getAmount() > 0 ? item : null);
          }
        }
        player.updateInventory();
      } else {
        player.sendMessage(this.noRepairMsg);
      }
    }
  }
}