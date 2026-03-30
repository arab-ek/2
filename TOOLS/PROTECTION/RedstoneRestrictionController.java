package dev.arab.TOOLS.PROTECTION;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RedstoneRestrictionController implements Listener {
    private final ConfigProtection config;
    private static final String BYPASS_PERM = "core.admin.redstone";

    public RedstoneRestrictionController(ConfigProtection config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!config.getConfig().getBoolean("settings.redstone.enabled", true)) return;

        Player player = event.getPlayer();
        if (player.hasPermission(BYPASS_PERM)) return;

        Material material = event.getBlock().getType();

        if (isAllowedInteractive(material)) return;

        if (isForbiddenRedstone(material)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage("redstone_blocked"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMinecartPlace(PlayerInteractEvent event) {
        if (!config.getConfig().getBoolean("settings.redstone.enabled", true)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        if (event.getPlayer().hasPermission(BYPASS_PERM)) return;

        Material type = item.getType();
        if (type == Material.CHEST_MINECART || type == Material.HOPPER_MINECART || type == Material.TNT_MINECART) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(config.getMessage("minecart_blocked"));
        }
    }

    private boolean isAllowedInteractive(Material m) {
        String n = m.name();
        List<String> allowed = config.getConfig().getStringList("settings.redstone.allowed_redstone_items");
        if (allowed.contains(n)) return true;

        return n.contains("BUTTON") || n.equals("LEVER");
    }

    private boolean isForbiddenRedstone(Material m) {
        String n = m.name();
        return n.contains("REDSTONE") || n.contains("PISTON") || n.contains("REPEATER") ||
                n.contains("COMPARATOR") || n.contains("HOPPER") || n.contains("OBSERVER") ||
                n.contains("DISPENSER") || n.contains("DROPPER") || n.contains("PRESSURE_PLATE") ||
                n.contains("TRIPWIRE") || n.contains("DAYLIGHT_DETECTOR") || n.contains("TARGET") ||
                n.contains("LECTERN") || n.contains("TNT") || n.contains("TRAPPED_CHEST") ||
                n.contains("SCULK_SENSOR") || n.contains("SCULK_SHRIEKER") || n.contains("CRAFTER");
    }
}