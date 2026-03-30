package dev.arab.TOOLS.HOME;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeGui implements Listener {
    private final HomeManager homeManager;
    private final ConfigHome config;

    public HomeGui(HomeManager homeManager, ConfigHome config) {
        this.homeManager = homeManager;
        this.config = config;
    }

    public void openGui(Player player) {
        Map<String, Location> homes = homeManager.getHomes(player.getUniqueId());

        int size = config.getConfig().getInt("gui.size", 27);
        String title = config.getConfig().getString("gui.title", "&8Twoje domy").replace("&", "§");

        Inventory inv = Bukkit.createInventory(null, size, title);

        int slot = 0;
        for (Map.Entry<String, Location> entry : homes.entrySet()) {
            if (slot >= size) break;

            ItemStack item = new ItemStack(Material.valueOf(config.getConfig().getString("gui.item_material", "WHITE_BED")));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(config.getConfig().getString("gui.item_name", "&aDom: &f{NAME}").replace("&", "§").replace("{NAME}", entry.getKey()));

                List<String> lore = new ArrayList<>();
                for (String line : config.getConfig().getStringList("gui.item_lore")) {
                    lore.add(line.replace("&", "§")
                            .replace("{X}", String.valueOf(entry.getValue().getBlockX()))
                            .replace("{Y}", String.valueOf(entry.getValue().getBlockY()))
                            .replace("{Z}", String.valueOf(entry.getValue().getBlockZ()))
                            .replace("{WORLD}", entry.getValue().getWorld().getName()));
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = config.getConfig().getString("gui.title", "&8Twoje domy").replace("&", "§");
        if (event.getView().getTitle().equals(title)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

            String displayName = clicked.getItemMeta().getDisplayName();
            Map<String, Location> homes = homeManager.getHomes(player.getUniqueId());

            for (String homeName : homes.keySet()) {
                String expectedName = config.getConfig().getString("gui.item_name", "&aDom: &f{NAME}").replace("&", "§").replace("{NAME}", homeName);
                if (displayName.equals(expectedName)) {
                    player.closeInventory();
                    player.teleport(homes.get(homeName));
                    player.sendMessage(config.getMessage("teleported").replace("{NAME}", homeName));
                    return;
                }
            }
        }
    }
}