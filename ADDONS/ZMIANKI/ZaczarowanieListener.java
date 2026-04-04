package dev.arab.ADDONS.ZMIANKI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ZaczarowanieListener implements Listener {
  private final Main plugin;
  private final ConfigZaczarowania config;
  private final NamespacedKey percentageKey;
  private final NamespacedKey zmiankaKey;
  private final Random random = new Random();
  private ItemStack cachedZmianka;
  private final Map<UUID, Long> cooldowns = new HashMap<>();
  private final Map<UUID, Integer> swordOriginalSlots = new HashMap<>();

  public ZaczarowanieListener(Main plugin, ConfigZaczarowania config) {
    this.plugin = plugin;
    this.config = config;
    this.percentageKey = new NamespacedKey(plugin, "extra_damage_percent");
    this.zmiankaKey = new NamespacedKey(plugin, "is_zmianka");
    this.reloadCache();
  }

  public void reloadCache() {
    this.cachedZmianka = this.config.getItemFromConfig("enchant_item");
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    ItemStack item = event.getItem();
    if (item != null && item.getType() != Material.AIR) {
      if (this.isZmianka(item)) {
        event.setCancelled(true);
        this.openGUI(event.getPlayer());
      }

    }
  }

  public void openGUI(Player player) {
    String title = this.config.getString("gui.title");
    Inventory inv = Bukkit.createInventory(null, 27, title);
    inv.setItem(11, this.config.getItemFromConfig("gui.item_action"));
    inv.setItem(13, this.config.getItemFromConfig("gui.item_slot"));
    inv.setItem(15, this.config.getItemFromConfig("gui.item_close"));
    player.openInventory(inv);
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    HumanEntity entity = event.getPlayer();
    if (entity instanceof Player player) {
      String title = this.config.getString("gui.title");
      if (event.getView().getTitle().equals(title)) {
        ItemStack sword = event.getInventory().getItem(13);
        if (sword != null && sword.getType() != Material.AIR && !sword.isSimilar(this.config.getItemFromConfig("gui.item_slot"))) {
          Integer originalSlot = this.swordOriginalSlots.remove(player.getUniqueId());
          if (originalSlot != null && player.getInventory().getItem(originalSlot) == null) {
            player.getInventory().setItem(originalSlot, sword);
          } else {
            player.getInventory().addItem(sword);
          }
        } else {
          this.swordOriginalSlots.remove(player.getUniqueId());
        }

      }
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    String title = this.config.getString("gui.title");
    if (event.getView().getTitle().equals(title)) {
      HumanEntity entity = event.getWhoClicked();
      if (entity instanceof Player player) {
        int slot = event.getRawSlot();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack sword;
        if (slot >= 27) {
          if (clickedItem != null && clickedItem.getType().toString().contains("SWORD") && (event.getAction().name().contains("PICKUP") || event.getAction().name().contains("MOVE_TO_OTHER_INVENTORY"))) {
            sword = event.getInventory().getItem(13);
            if (sword != null && sword.getType() != Material.AIR && !sword.isSimilar(this.config.getItemFromConfig("gui.item_slot"))) {
              event.setCancelled(true);
            } else {
              event.setCancelled(true);
              this.swordOriginalSlots.put(player.getUniqueId(), event.getSlot());
              event.getInventory().setItem(13, clickedItem.clone());
              clickedItem.setAmount(0);
            }
          } else {
            event.setCancelled(true);
          }
        } else {
          event.setCancelled(true);
          if (slot == 15) {
            player.closeInventory();
          } else {
            if (slot == 11) {
              sword = event.getInventory().getItem(13);
              if (sword == null || sword.getType() == Material.AIR || sword.isSimilar(this.config.getItemFromConfig("gui.item_slot"))) {
                String msg = this.config.getString("settings.messages.no_sword");
                if (msg != null && !msg.isEmpty()) {
                  player.sendMessage(msg);
                }

                return;
              }

              long now = System.currentTimeMillis();
              Long lastRoll = this.cooldowns.get(player.getUniqueId());
              if (lastRoll != null && now - lastRoll < 0L) {
                return;
              }

              this.cooldowns.put(player.getUniqueId(), now);
              boolean consumed = false;

              for(int i = 0; i < player.getInventory().getSize(); ++i) {
                ItemStack is = player.getInventory().getItem(i);
                if (this.isZmianka(is)) {
                  is.setAmount(is.getAmount() - 1);
                  consumed = true;
                  break;
                }
              }

              if (!consumed) {
                String msg = this.config.getString("settings.messages.no_material");
                if (msg != null && !msg.isEmpty()) {
                  player.sendMessage(msg);
                }

                return;
              }

              this.enchantSword(player, sword, event.getInventory());
            }

          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private int rollWeighted() {
    List<?> rawList = this.config.getConfig().getList("settings.chance_ranges");
    if (rawList != null && !rawList.isEmpty()) {
      List<Map<String, Object>> rangesList = (List<Map<String, Object>>) rawList;
      int totalChance = 0;

      for (Map<String, Object> entry : rangesList) {
        Object chanceObj = entry.get("chance");
        if (chanceObj instanceof Number n) {
          totalChance += n.intValue();
        } else {
          totalChance += 1;
        }
      }

      if (totalChance <= 0) return 0;

      int roll = this.random.nextInt(totalChance);
      int cumulative = 0;
      Map<String, Object> selectedEntry = null;

      for (Map<String, Object> entry : rangesList) {
        Object chanceObj = entry.get("chance");
        int chance = (chanceObj instanceof Number n) ? n.intValue() : 1;

        cumulative += chance;
        if (roll < cumulative) {
          selectedEntry = entry;
          break;
        }
      }

      if (selectedEntry != null) {
        Object minObj = selectedEntry.get("min");
        Object maxObj = selectedEntry.get("max");

        int min = (minObj instanceof Number n) ? n.intValue() : 0;
        int max = (maxObj instanceof Number n) ? n.intValue() : 0;

        if (max < min) max = min;
        return min + this.random.nextInt(max - min + 1);
      }
      return 0;
    } else {
      return -15 + this.random.nextInt(67);
    }
  }

  private void updateActionButton(Inventory inv) {
    ItemStack actionItem = this.config.getItemFromConfig("gui.item_action");
    ItemStack sword = inv.getItem(13);
    if (sword != null && sword.getType() != Material.AIR && !sword.isSimilar(this.config.getItemFromConfig("gui.item_slot"))) {
      ItemMeta swordMeta = sword.getItemMeta();
      int procent = 0;
      if (swordMeta != null) {
        PersistentDataContainer pdc = swordMeta.getPersistentDataContainer();
        procent = (int)Math.round(pdc.getOrDefault(this.percentageKey, PersistentDataType.DOUBLE, 0.0D));
      }

      String procentStr = String.valueOf(procent);
      String template;
      if (procent < 0) {
        template = this.config.getString("settings.lore_templates.low");
      } else if (procent <= 30) {
        template = this.config.getString("settings.lore_templates.normal");
      } else if (procent <= 45) {
        template = this.config.getString("settings.lore_templates.high");
      } else {
        template = this.config.getString("settings.lore_templates.very_high");
      }

      String resultLine = template.replace("{procent}", String.valueOf(Math.abs(procent)));
      List<String> configLore = this.config.getStringList("gui.item_action.lore");
      List<String> newLore = new ArrayList<>();
      newLore.add(" ");
      newLore.add(" §8» §7Ostatnie wylosowane:");
      newLore.add(" §8» §f" + resultLine);

      for (String line : configLore) {
        newLore.add(line.replace("{procent}", procentStr));
      }

      ItemMeta actionMeta = actionItem.getItemMeta();
      if (actionMeta != null) {
        actionMeta.setLore(newLore);
        actionItem.setItemMeta(actionMeta);
      }

      inv.setItem(11, actionItem);
    } else {
      inv.setItem(11, actionItem);
    }
  }

  private void enchantSword(Player player, ItemStack sword, Inventory inv) {
    int extraPercent = this.rollWeighted();
    ItemMeta meta = sword.getItemMeta();
    if (meta != null) {
      PersistentDataContainer pdc = meta.getPersistentDataContainer();
      pdc.set(this.percentageKey, PersistentDataType.DOUBLE, (double)extraPercent);
      List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

      lore.removeIf((line) -> {
        String stripped = ChatUtils.stripColor(line);
        return stripped.contains("Dodatkowe obrażenia") || stripped.contains("Dodatkowe obrazenia");
      });

      while(!lore.isEmpty() && (lore.get(0).isEmpty() || lore.get(0).trim().isEmpty())) {
        lore.remove(0);
      }

      String template;
      if (extraPercent < 0) {
        template = this.config.getString("settings.lore_templates.low");
      } else if (extraPercent <= 30) {
        template = this.config.getString("settings.lore_templates.normal");
      } else if (extraPercent <= 45) {
        template = this.config.getString("settings.lore_templates.high");
      } else {
        template = this.config.getString("settings.lore_templates.very_high");
      }

      String loreLine = template.replace("{procent}", String.valueOf(Math.abs(extraPercent)));
      List<String> newLore = new ArrayList<>();
      newLore.add(loreLine);
      newLore.add("");
      newLore.addAll(lore);
      meta.setLore(newLore);
      sword.setItemMeta(meta);
      this.updateActionButton(inv);
    }
  }

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent event) {
    Entity entity = event.getDamager();
    if (entity instanceof Player player) {
      ItemStack item = player.getInventory().getItemInMainHand();
      if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
          PersistentDataContainer pdc = meta.getPersistentDataContainer();
          Double extraPercent = pdc.get(this.percentageKey, PersistentDataType.DOUBLE);
          if (extraPercent == null && meta.hasLore()) {
            for (String line : meta.getLore()) {
              String stripped = ChatUtils.stripColor(line).toLowerCase();
              if (stripped.contains("dodatkowe obrażenia") || stripped.contains("dodatkowe obrazenia") || stripped.contains("zaczarowanie")) {
                try {
                  String replaced = stripped.replace("dodatkowe obrażenia", "").replace("dodatkowe obrazenia", "").replace("zaczarowanie", "").replace(":", "").replace("%", "").replace("+", "").trim();
                  if (!replaced.isEmpty()) {
                    extraPercent = Double.parseDouble(replaced);
                    pdc.set(this.percentageKey, PersistentDataType.DOUBLE, extraPercent);
                    item.setItemMeta(meta);
                    break;
                  }
                } catch (NumberFormatException ignored) {}
              }
            }
          }

          if (extraPercent != null && extraPercent != 0.0D) {
            double originalDamage = event.getDamage();
            event.setDamage(originalDamage + originalDamage * (extraPercent / 100.0D));
          }
        }
      }
    }
  }

  private boolean isZmianka(ItemStack item) {
    if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
      ItemMeta meta = item.getItemMeta();
      if (meta == null) {
        return false;
      } else {
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(this.zmiankaKey, PersistentDataType.BYTE)) {
          return true;
        } else {
          if (this.cachedZmianka == null) {
            this.reloadCache();
          }

          if (this.cachedZmianka != null && item.getType() == this.cachedZmianka.getType() && meta.hasDisplayName() && this.cachedZmianka.hasItemMeta() && this.cachedZmianka.getItemMeta().hasDisplayName() && meta.getDisplayName().equals(this.cachedZmianka.getItemMeta().getDisplayName())) {
            pdc.set(this.zmiankaKey, PersistentDataType.BYTE, (byte)1);
            item.setItemMeta(meta);
            return true;
          } else {
            return false;
          }
        }
      }
    } else {
      return false;
    }
  }
}