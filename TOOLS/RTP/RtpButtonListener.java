package dev.arab.TOOLS.RTP;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class RtpButtonListener implements Listener {
    private final RtpManager rtpManager;

    public RtpButtonListener(RtpManager rtpManager) {
        this.rtpManager = rtpManager;
    }

    @EventHandler
    public void onButtonClick(PlayerInteractEvent event) {
        // Interesuje nas tylko prawy klik na blok
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        // Sprawdzamy czy to kamienny przycisk
        if (clickedBlock.getType() != Material.STONE_BUTTON) return;

        // Pobieramy blok pod przyciskiem (BlockFace.DOWN)
        Block blockBelow = clickedBlock.getRelative(BlockFace.DOWN);

        // Sprawdzamy czy blok poniżej to Gąbka
        if (blockBelow.getType() == Material.SPONGE) {
            Player player = event.getPlayer();

            // Możemy sprawdzić permisję dla przycisku jeśli chcesz (opcjonalnie)
            // if (!player.hasPermission("core.rtp.button")) return;

            // Wywołujemy teleportację z enforceCooldown = false (lub true jeśli chcesz cooldown też na przycisk)
            rtpManager.performRtp(player, false);

            // Opcjonalnie: event.setCancelled(true); aby nie klikało się "nic innego"
        }
    }
}