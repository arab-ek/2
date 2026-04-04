package dev.arab.ADDONS.INCOGNITO;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class IncognitoManager {
  private final JavaPlugin plugin;
  private final ConfigManager configManager;
  private final Map<UUID, String> incognitoNicks = new HashMap<>();
  private final Map<UUID, String> originalNames = new HashMap<>();
  private SkinsRestorer skinsRestorer;
  private boolean tabEnabled;

  public IncognitoManager(JavaPlugin plugin, ConfigManager configManager) {
    this.plugin = plugin;
    this.configManager = configManager;

    try {
      if (Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer")) {
        this.skinsRestorer = SkinsRestorerProvider.get();
      }

      this.tabEnabled = Bukkit.getPluginManager().isPluginEnabled("TAB");
    } catch (Exception ignored) {
    }
  }

  public void toggleIncognito(Player player) {
    if (this.incognitoNicks.containsKey(player.getUniqueId())) {
      this.disableIncognito(player);
      player.sendMessage("§4§l✘ §8× §cTryb incognito został wyłączony!");
    } else {
      this.enableIncognito(player);
      player.sendMessage("§2§l✔ §8× §aTryb incognito został włączony!");
    }
  }

  private void enableIncognito(Player player) {
    String originalName = player.getName();
    this.originalNames.put(player.getUniqueId(), originalName);
    String randomNick = this.generateRandomNick();
    this.incognitoNicks.put(player.getUniqueId(), randomNick);
    this.setGameProfileName(player, randomNick);
    player.setDisplayName(randomNick);
    this.applySkin(player);
    this.updateTab(player, randomNick);
    this.refreshPlayer(player);
  }

  private String generateRandomNick() {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder sb = new StringBuilder();
    Random rnd = new Random();

    for(int i = 0; i < 12; ++i) {
      sb.append(chars.charAt(rnd.nextInt(chars.length())));
    }

    return sb.toString();
  }

  private void disableIncognito(Player player) {
    String originalName = this.originalNames.remove(player.getUniqueId());
    if (originalName == null) {
      originalName = player.getName();
    }

    this.incognitoNicks.remove(player.getUniqueId());
    this.setGameProfileName(player, originalName);
    player.setDisplayName(originalName);
    this.resetSkin(player);
    this.updateTab(player, null);
    this.refreshPlayer(player);
  }

  private void updateTab(Player player, String name) {
    if (this.tabEnabled) {
      try {
        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer != null) {
          if (name != null) {
            TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, "");
            TabAPI.getInstance().getNameTagManager().setSuffix(tabPlayer, "");

            try {
              Method setCustomName = tabPlayer.getClass().getMethod("setCustomName", String.class);
              setCustomName.invoke(tabPlayer, name);
            } catch (Exception ignored) {
            }
          } else {
            TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, null);
            TabAPI.getInstance().getNameTagManager().setSuffix(tabPlayer, null);
          }
        }
      } catch (Exception e) {
        Bukkit.getLogger().warning("Bląd tab API: " + e.getMessage());
      }
    }
  }

  private void setGameProfileName(Player player, String name) {
    try {
      // Pobieramy GameProfile bezpośrednio z Bukkita (CraftPlayer) by uniknąć zmian ścieżek NMS w 1.19.3+
      Object profile = player.getClass().getMethod("getProfile").invoke(player);
      Field nameField = this.findField(profile.getClass(), "name");
      if (nameField != null) {
        nameField.setAccessible(true);
        nameField.set(profile, name);
      }
    } catch (Exception e) {
      Bukkit.getLogger().warning("Błąd podczas zmiany nazwy w GameProfile: " + e.getMessage());
    }
  }

  private void applySkin(Player player) {
    if (this.skinsRestorer != null) {
      String skinValue = this.configManager.getSkinValue();
      String skinSignature = this.configManager.getSkinSignature();
      if (skinValue != null && !skinValue.isEmpty()) {
        try {
          SkinProperty skinProperty = SkinProperty.of(skinValue, skinSignature);
          String skinName = "incognito_" + player.getUniqueId().toString();
          SkinStorage skinStorage = this.skinsRestorer.getSkinStorage();
          skinStorage.setCustomSkinData(skinName, skinProperty);
          PlayerStorage playerStorage = this.skinsRestorer.getPlayerStorage();
          playerStorage.setSkinIdOfPlayer(player.getUniqueId(), SkinIdentifier.ofCustom(skinName));
          this.skinsRestorer.getSkinApplier(Player.class).applySkin(player);
        } catch (Exception e) {
          Bukkit.getLogger().warning("Bląd SkinRestorera: " + e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }

  private void resetSkin(Player player) {
    if (this.skinsRestorer != null) {
      try {
        this.skinsRestorer.getPlayerStorage().removeSkinIdOfPlayer(player.getUniqueId());
        this.skinsRestorer.getSkinApplier(Player.class).applySkin(player);
      } catch (Exception e) {
        Bukkit.getLogger().warning("Błąd SkinRestorera reset: " + e.getMessage());
      }
    }
  }

  public boolean isIncognito(UUID uuid) {
    return this.incognitoNicks.containsKey(uuid);
  }

  public String getIncognitoNick(UUID uuid) {
    return this.incognitoNicks.get(uuid);
  }

  private void refreshPlayer(Player player) {
    for (Player online : Bukkit.getOnlinePlayers()) {
      if (!online.equals(player)) {
        online.hidePlayer(this.plugin, player);
        online.showPlayer(this.plugin, player);
      }
    }
    this.refreshSelf(player);
  }

  private void refreshSelf(Player player) {
    // Od wersji 1.19.3 pakiety PlayerInfo zostały całkowicie przebudowane.
    // Skoro używasz SkinsRestorer, on sam wysyła odpowiednie pakiety odświeżające do gracza.
    // Robimy tu więc tylko mikro-teleport, by wymusić doczytanie modelu przez klienta.
    Bukkit.getScheduler().runTask(this.plugin, () -> {
      Location loc = player.getLocation();
      player.teleport(loc.clone().add(0.0D, 0.01D, 0.0D));
      player.teleport(loc);
    });
  }

  private Field findField(Class<?> clazz, String fieldName) {
    while(clazz != null && clazz != Object.class) {
      try {
        return clazz.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      }
    }
    return null;
  }
}