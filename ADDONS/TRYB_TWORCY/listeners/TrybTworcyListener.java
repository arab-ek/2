package dev.arab.ADDONS.TRYB_TWORCY.listeners;

import dev.arab.ADDONS.TRYB_TWORCY.managers.TrybTworcyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class TrybTworcyListener implements Listener {
  private final TrybTworcyManager manager;

  public TrybTworcyListener(TrybTworcyManager manager) {
    this.manager = manager;
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player victim = event.getEntity();
    Player killer = victim.getKiller();
    boolean keepInventory = false;

    if (this.manager.hasModeEnabled(victim)) {
      keepInventory = true;
    } else if (killer != null && this.manager.hasModeEnabled(killer)) {
      keepInventory = true;
    }

    if (keepInventory) {
      event.setKeepInventory(true);
      event.setKeepLevel(true);
      event.getDrops().clear();
      event.setDroppedExp(0);
    }
  }
}