package dev.arab.TOOLS.HOME;

import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeManager {
    private final Main plugin;
    private File file;
    private FileConfiguration data;

    // UUID gracza -> (Nazwa domu -> Lokalizacja)
    private final Map<UUID, Map<String, Location>> playerHomes = new HashMap<>();

    public HomeManager(Main plugin) {
        this.plugin = plugin;
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        this.file = new File(dataFolder, "homes_data.yml");
        if (!this.file.exists()) {
            try { this.file.createNewFile(); } catch (IOException ignored) {}
        }
        this.data = YamlConfiguration.loadConfiguration(this.file);
        loadData();
    }

    private void loadData() {
        for (String uuidStr : data.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<String, Location> homes = new HashMap<>();

            for (String homeName : data.getConfigurationSection(uuidStr).getKeys(false)) {
                String path = uuidStr + "." + homeName;
                World world = Bukkit.getWorld(data.getString(path + ".world", "world"));
                if (world == null) continue;
                double x = data.getDouble(path + ".x");
                double y = data.getDouble(path + ".y");
                double z = data.getDouble(path + ".z");
                float yaw = (float) data.getDouble(path + ".yaw");
                float pitch = (float) data.getDouble(path + ".pitch");

                homes.put(homeName, new Location(world, x, y, z, yaw, pitch));
            }
            playerHomes.put(uuid, homes);
        }
    }

    public void saveData() {
        // Czyścimy starą konfigurację, by usunąć skasowane domy
        for (String key : data.getKeys(false)) data.set(key, null);

        for (Map.Entry<UUID, Map<String, Location>> entry : playerHomes.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Location> home : entry.getValue().entrySet()) {
                String path = uuidStr + "." + home.getKey();
                Location loc = home.getValue();
                data.set(path + ".world", loc.getWorld().getName());
                data.set(path + ".x", loc.getX());
                data.set(path + ".y", loc.getY());
                data.set(path + ".z", loc.getZ());
                data.set(path + ".yaw", loc.getYaw());
                data.set(path + ".pitch", loc.getPitch());
            }
        }
        try { data.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public Map<String, Location> getHomes(UUID uuid) {
        return playerHomes.getOrDefault(uuid, new HashMap<>());
    }

    public Location getHome(UUID uuid, String name) {
        return getHomes(uuid).get(name);
    }

    public void setHome(UUID uuid, String name, Location loc) {
        playerHomes.computeIfAbsent(uuid, k -> new HashMap<>()).put(name, loc);
    }

    public void deleteHome(UUID uuid, String name) {
        if (playerHomes.containsKey(uuid)) {
            playerHomes.get(uuid).remove(name);
        }
    }

    public int getHomeLimit(Player player) {
        // Działa od tyłu, sprawdzając najwyższy dostępny limit.
        // Wystarczy nadać permisję np. core.home.limit.5
        for (int i = 50; i >= 1; i--) {
            if (player.hasPermission("core.home.limit." + i)) return i;
        }
        return 3; // Domyślny limit bez permisji
    }
}