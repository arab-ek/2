package dev.arab.ADDONS.TRYB_TWORCY.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import dev.arab.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class TrybTworcyManager {
  private final Main plugin;
  private final Set<UUID> activePlayers = new HashSet<>();
  private final File playersFile;
  private FileConfiguration config;
  private FileConfiguration playersConfig;

  public TrybTworcyManager(Main plugin) {
    this.plugin = plugin;
    this.playersFile = new File(plugin.getDataFolder(), "players.yml");
    loadConfig();
    loadPlayers();
  }

  public void loadConfig() {
    File configFile = new File(this.plugin.getDataFolder(), "addons/config-tworca.yml");
    if (!configFile.exists()) {
      this.plugin.saveResource("addons/config-tworca.yml", false);
    }
    this.config = YamlConfiguration.loadConfiguration(configFile);
  }

  private void loadPlayers() {
    if (!this.playersFile.exists()) {
      this.playersConfig = new YamlConfiguration();
      return;
    }
    this.playersConfig = YamlConfiguration.loadConfiguration(this.playersFile);
    for (String uuidStr : this.playersConfig.getStringList("active-players")) {
      try {
        this.activePlayers.add(UUID.fromString(uuidStr));
      } catch (IllegalArgumentException ignored) {}
    }
  }

  public void savePlayers() {
    this.playersConfig.set("active-players", this.activePlayers.stream().map(UUID::toString).collect(Collectors.toList()));
    try {
      this.playersConfig.save(this.playersFile);
    } catch (IOException e) {
      this.plugin.getLogger().severe("Nie udalo sie zapisac players.yml!");
    }
  }

  public void toggleMode(Player player) {
    UUID uuid = player.getUniqueId();
    if (this.activePlayers.contains(uuid)) {
      this.activePlayers.remove(uuid);
      sendMessage(player, "messages.disabled", "&4&l✖ &7Tryb twórcy został &cwyłączony&7!");
    } else {
      this.activePlayers.add(uuid);
      sendMessage(player, "messages.enabled", "&a&l✔ &fTryb twórcy został &awłączony&f!");
    }
    savePlayers();
  }

  public boolean hasModeEnabled(Player player) {
    return this.activePlayers.contains(player.getUniqueId());
  }

  public FileConfiguration getConfig() {
    return this.config;
  }

  public void cleanup() {
    this.activePlayers.clear();
  }

  private void sendMessage(Player player, String path, String def) {
    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString(path, def)));
  }
}