package dev.arab.EVENTOWKICORE.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import dev.arab.Main;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.EVENTOWKICORE.utils.EquipmentCacheManager.PlayerEquipment;
import dev.arab.SZAFACORE.data.PetData;
import dev.arab.SZAFACORE.pets.pety.SowaPet;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("deprecation")
public class ActionBarTask extends BukkitRunnable {
  private final Main plugin;
  private final Map<String, BaseComponent[]> nameCache = new HashMap<>();
  private final Map<String, BaseComponent[]> labelCache = new HashMap<>();
  private final NamespacedKey itemKey;
  private final NamespacedKey chargeKey;
  private BaseComponent[] elytraChargePrefix;
  private BaseComponent[] elytraChargeSuffix;
  private long ticks = 0L;
  private final Map<UUID, BaseComponent[]> lastSentMessage = new HashMap<>();
  private BaseComponent[] trybTworcyMsg;
  private BaseComponent[] separator;
  private final Map<String, BaseComponent[]> formattedCooldownNames = new HashMap<>();

  public ActionBarTask(Main plugin) {
    this.plugin = plugin;
    this.itemKey = new NamespacedKey(plugin, "event_item_id");
    this.chargeKey = new NamespacedKey(plugin, "elytra_charge");
    this.cacheNames();
  }

  private void cacheNames() {
    this.nameCache.clear();
    this.labelCache.clear();

    ConfigurationSection meta = this.plugin.getConfig().getConfigurationSection("meta");
    if (meta != null) {
      for (String key : meta.getKeys(false)) {
        this.nameCache.put(key, TextComponent.fromLegacyText(ChatUtils.color(meta.getString(key + ".name", key))));
      }
    }

    ConfigurationSection itemsMsg = this.plugin.getMessagesConfig().getConfigurationSection("items");
    if (itemsMsg != null) {
      for (String parentKey : itemsMsg.getKeys(false)) {
        ConfigurationSection sub = itemsMsg.getConfigurationSection(parentKey);
        if (sub != null) {
          for (String subKey : sub.getKeys(false)) {
            String label = sub.getString(subKey + ".label");
            if (label != null) {
              this.labelCache.put(parentKey + "." + subKey, TextComponent.fromLegacyText(ChatUtils.color(label)));
            }
          }
        }
      }
    }

    String elytraMsg = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.wzmocniona_elytra.charge_message", "&bWzmocniona Elytra załadowana w &3%percentage%%"));
    if (elytraMsg.contains("%percentage%")) {
      String[] parts = elytraMsg.split("%percentage%", 2);
      this.elytraChargePrefix = TextComponent.fromLegacyText(parts[0]);
      this.elytraChargeSuffix = parts.length > 1 ? TextComponent.fromLegacyText(parts[1]) : new BaseComponent[0];
    } else {
      this.elytraChargePrefix = TextComponent.fromLegacyText(elytraMsg);
      this.elytraChargeSuffix = new BaseComponent[0];
    }

    this.trybTworcyMsg = TextComponent.fromLegacyText(ChatUtils.color(this.plugin.getTrybTworcyManager().getConfig().getString("actionbar.text", "&8※ &f&lᴛʀʏʙ &c&lᴛᴡᴏʀᴄʏ: &a&l✔ &8※")));
    this.separator = TextComponent.fromLegacyText(" §8• ");
  }

  public void run() {
    this.ticks += 5L;
    if (this.ticks % 100L == 0L) {
      this.cacheNames();
      this.formattedCooldownNames.clear();
    }

    boolean updateElytra = this.ticks % 20L == 0L;

    for (Player player : Bukkit.getOnlinePlayers()) {
      UUID uuid = player.getUniqueId();
      ComponentBuilder builder = new ComponentBuilder("");

      this.plugin.getCooldownManager().forEachActiveCooldown(uuid, (key, remainingMs) -> {
        if (key != null && !key.isEmpty() && !key.endsWith("_blocked")) {
          long seconds = remainingMs / 1000L;
          long ms = remainingMs % 1000L;

          BaseComponent[] displayName = this.formattedCooldownNames.computeIfAbsent(key, (k) -> {
            String itemId = k.contains(":") ? k.split(":")[0] : k;
            BaseComponent[] name = this.nameCache.get(itemId);

            if (itemId.contains(".")) {
              int dot = itemId.indexOf('.');
              String pk = itemId.substring(0, dot);
              String sk = itemId.substring(dot + 1);
              BaseComponent[] parentNameComp = this.nameCache.get(pk);
              String parentName = parentNameComp != null ? TextComponent.toLegacyText(parentNameComp) : pk;
              BaseComponent[] labelComp = this.labelCache.get(itemId);
              String label = labelComp != null ? TextComponent.toLegacyText(labelComp) : sk;
              return TextComponent.fromLegacyText(parentName + " §8[§f" + label + "§8]");
            } else {
              return name != null ? name : TextComponent.fromLegacyText(itemId);
            }
          });

          if (builder.getParts().size() > 1) {
            builder.append(this.separator, FormatRetention.NONE);
          }

          builder.append(displayName, FormatRetention.NONE);
          StringBuilder sb = new StringBuilder(" §8(§f");
          sb.append(seconds).append("sek ");
          if (ms < 100L) {
            sb.append('0');
          }

          if (ms < 10L) {
            sb.append('0');
          }

          sb.append(ms).append("ms§8)");
          builder.append(TextComponent.fromLegacyText(sb.toString()), FormatRetention.NONE);
        }
      });

      PlayerEquipment equipment = this.plugin.getEquipmentCacheManager().getEquipment(uuid);
      if (equipment != null && equipment.hasWzmocnionaElytra) {
        double charge = equipment.elytraCharge;
        if (updateElytra && player.isGliding() && equipment.elytraSlot != null) {
          ItemStack chest = player.getInventory().getItem(equipment.elytraSlot);
          if (chest != null && chest.getType() == Material.ELYTRA) {
            ItemMeta meta = chest.getItemMeta();
            if (meta != null) {
              double speed = player.getVelocity().length();
              double newCharge = Math.min(100.0D, charge + speed * 3.0D);
              if (Math.abs(newCharge - charge) > 0.01D) {
                charge = newCharge;
                meta.getPersistentDataContainer().set(this.chargeKey, PersistentDataType.DOUBLE, newCharge);
                chest.setItemMeta(meta);
                equipment.elytraCharge = newCharge;
              }
            }
          }
        }

        if (charge > 0.0D) {
          if (builder.getParts().size() > 1) {
            builder.append(this.separator, FormatRetention.NONE);
          }

          double rounded = (double)Math.round(charge * 10.0D) / 10.0D;
          builder.append(this.elytraChargePrefix, FormatRetention.NONE).append(String.valueOf(rounded), FormatRetention.NONE).append(this.elytraChargeSuffix, FormatRetention.NONE);
        }
      }

      PetData petData = this.plugin.getPetManager().getActivePet(player);
      if (petData != null && "sowa".equals(petData.getPetId())) {
        String targetInfo = SowaPet.getTargetInfo(uuid);
        if (targetInfo != null) {
          if (builder.getParts().size() > 1) {
            builder.append(this.separator, FormatRetention.NONE);
          }

          builder.append(TextComponent.fromLegacyText(targetInfo), FormatRetention.NONE);
        }
      }

      if (this.plugin.getTrybTworcyManager().hasModeEnabled(player)) {
        if (builder.getParts().size() > 1) {
          builder.append(this.separator, FormatRetention.NONE);
        }

        builder.append(this.trybTworcyMsg, FormatRetention.NONE);
      }

      BaseComponent[] current = builder.create();
      BaseComponent[] last = this.lastSentMessage.get(uuid);
      boolean refresh = this.ticks % 40L == 0L;

      if (current.length == 0 || (current.length == 1 && current[0].toPlainText().isEmpty())) {
        if (last != null && last.length > 0) {
          player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
          this.lastSentMessage.put(uuid, null);
        }
        continue;
      }

      if (refresh || last == null || !this.areEqual(current, last)) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, current);
        this.lastSentMessage.put(uuid, current);
      }
    }
  }

  private boolean areEqual(BaseComponent[] a, BaseComponent[] b) {
    if (a == b) {
      return true;
    } else if (a != null && b != null) {
      return a.length == b.length && TextComponent.toLegacyText(a).equals(TextComponent.toLegacyText(b));
    } else {
      return false;
    }
  }
}