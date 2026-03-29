package dev.arab.EVENTOWKICORE.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import dev.arab.EVENTOWKICORE.inventory.SakiewkaInventory;
import dev.arab.EVENTOWKICORE.utils.ChatUtils;
import dev.arab.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class InventoryListener implements Listener {
  private final Main plugin;
  
  private static NamespacedKey EVENT_KEY;
  
  private static NamespacedKey UID_KEY;
  
  private static NamespacedKey COSTUME_KEY;
  
  private static NamespacedKey PET_KEY;
  
  private static NamespacedKey PARROT_KEY;
  
  private final Map<UUID, Long> lastScan = new HashMap<>();
  
  public InventoryListener(Main plugin) {
    this.plugin = plugin;
    if (EVENT_KEY == null) {
      EVENT_KEY = new NamespacedKey((Plugin)plugin, "event_item_id");
      UID_KEY = new NamespacedKey((Plugin)plugin, "event_item_uid");
      COSTUME_KEY = new NamespacedKey((Plugin)plugin, "costume_id");
      PET_KEY = new NamespacedKey((Plugin)plugin, "pet_id");
      PARROT_KEY = new NamespacedKey((Plugin)plugin, "parrot_id");
    } 
  }
  
  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    scanInventory(event.getPlayer());
  }
  
  @EventHandler
  public void onInventoryOpen(InventoryOpenEvent event) {
    HumanEntity humanEntity = event.getPlayer();
    if (humanEntity instanceof Player) {
      Player player = (Player)humanEntity;
      long now = System.currentTimeMillis();
      if (now - ((Long)this.lastScan.getOrDefault(player.getUniqueId(), Long.valueOf(0L))).longValue() > 5000L) {
        scanInventory(player);
        this.lastScan.put(player.getUniqueId(), Long.valueOf(now));
      } 
    } 
  }
  
  private void scanInventory(Player player) {
    ItemStack[] contents = player.getInventory().getContents();
    boolean inventoryChanged = false;
    for (int i = 0; i < contents.length; i++) {
      ItemStack item = contents[i];
      if (item != null && !item.getType().isAir() && item.hasItemMeta()) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
          if (item.getAmount() > 1) {
            String eid = (String)meta.getPersistentDataContainer().get(EVENT_KEY, PersistentDataType.STRING);
            if ("sakiewka_dropu".equals(eid)) {
              int extra = item.getAmount() - 1;
              item.setAmount(1);
              contents[i] = item;
              inventoryChanged = true;
              for (int j = 0; j < extra; j++) {
                ItemStack drop = item.clone();
                ItemMeta dropMeta = drop.getItemMeta();
                if (dropMeta != null) {
                  dropMeta.getPersistentDataContainer().set(UID_KEY, PersistentDataType.STRING, UUID.randomUUID().toString());
                  drop.setItemMeta(dropMeta);
                } 
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
              } 
            } 
          } 
          this.plugin.getEventItemManager().getItem(item);
        } 
      } 
    } 
    if (inventoryChanged)
      player.getInventory().setContents(contents); 
  }
  
  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player))
      return; 
    Player player = (Player)event.getWhoClicked();
    ItemStack currentItem = event.getCurrentItem();
    ItemStack cursorItem = event.getCursor();
    boolean currentMeta = (currentItem != null && currentItem.hasItemMeta());
    boolean cursorMeta = (cursorItem != null && cursorItem.hasItemMeta());
    if (event.getRawSlot() == 45) {
      ItemStack itemToPlace = (event.getClick() == ClickType.NUMBER_KEY) ? player.getInventory().getItem(event.getHotbarButton()) : cursorItem;
      if (isForbiddenOffhandItem(itemToPlace)) {
        event.setCancelled(true);
        return;
      } 
    } 
    if (event.getClick() == ClickType.SWAP_OFFHAND && (isForbiddenOffhandItem(currentItem) || isForbiddenOffhandItem(player.getInventory().getItemInOffHand()))) {
      event.setCancelled(true);
      return;
    } 
    if (event.getClick().isShiftClick() && isForbiddenOffhandItem(currentItem)) {
      InventoryType type = event.getInventory().getType();
      if (type == InventoryType.CRAFTING || type == InventoryType.PLAYER) {
        event.setCancelled(true);
        return;
      } 
    } 
    InventoryAction action = event.getAction();
    if ((action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_SOME || action == InventoryAction.PLACE_ONE || action == InventoryAction.SWAP_WITH_CURSOR) && isSakiewka(cursorItem) && (isSakiewka(currentItem) || (currentItem != null && currentItem.getType() != Material.AIR && currentItem.isSimilar(cursorItem)))) {
      event.setCancelled(true);
      return;
    } 
    if (action == InventoryAction.COLLECT_TO_CURSOR && isSakiewka(cursorItem)) {
      event.setCancelled(true);
      return;
    } 
    if ((action == InventoryAction.HOTBAR_MOVE_AND_READD || action == InventoryAction.HOTBAR_SWAP) && event.getClick() == ClickType.NUMBER_KEY) {
      ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
      if (isSakiewka(hotbarItem) && (isSakiewka(currentItem) || (currentItem != null && currentItem.getType() != Material.AIR && currentItem.isSimilar(hotbarItem)))) {
        event.setCancelled(true);
        return;
      } 
    } 
    String title = event.getView().getTitle();
    String menuTitle = ChatUtils.color(this.plugin.getMessagesConfig().getString("gui.title", "Custom przedmioty anarchia"));
    if (title.equals(menuTitle))
      return; 
    String sakTitle = ChatUtils.color(this.plugin.getMessagesConfig().getString("items.sakiewka_dropu.inventory_title", "&aSakiewka Dropu"));
    if (title.equals(sakTitle)) {
      event.setCancelled(true);
      ItemStack clickedItem = event.getCurrentItem();
      if (clickedItem == null || clickedItem.getType() == Material.AIR)
        return; 
      int rawSlot = event.getRawSlot();
      if (rawSlot == 49) {
        handleSakiewkaReceiveAll(player);
      } else if (rawSlot >= 0 && rawSlot < 45) {
        handleSakiewkaSingleExtraction(player, clickedItem, rawSlot);
      } 
    } 
  }
  
  private ItemStack findSakiewka(Player player) {
    ItemStack main = player.getInventory().getItemInMainHand();
    if (isSakiewka(main))
      return main; 
    ItemStack off = player.getInventory().getItemInOffHand();
    return isSakiewka(off) ? off : main;
  }
  
  private void handleSakiewkaSingleExtraction(Player player, ItemStack clickedItem, int slot) {
    ItemStack pouch = findSakiewka(player);
    SakiewkaInventory sakiewka = new SakiewkaInventory(this.plugin);
    List<ItemStack> items = sakiewka.getStoredItems(pouch);
    if (slot >= items.size())
      return; 
    ItemStack itemToTake = items.get(slot);
    if (itemToTake == null || itemToTake.getType() == Material.AIR)
      return; 
    ItemStack singleDrop = itemToTake.clone();
    singleDrop.setAmount(1);
    player.getInventory().addItem(new ItemStack[] { singleDrop }).values().forEach(remaining -> player.getWorld().dropItemNaturally(player.getLocation(), remaining));
    if (itemToTake.getAmount() > 1) {
      itemToTake.setAmount(itemToTake.getAmount() - 1);
    } else {
      items.remove(slot);
    } 
    sakiewka.saveItems(pouch, items);
    Inventory inv = player.getOpenInventory().getTopInventory();
    for (int i = 0; i < 45; i++) {
      if (i < items.size()) {
        inv.setItem(i, items.get(i));
      } else {
        inv.setItem(i, null);
      } 
    } 
  }
  
  private void handleSakiewkaReceiveAll(Player player) {
    ItemStack pouch = findSakiewka(player);
    SakiewkaInventory sakiewka = new SakiewkaInventory(this.plugin);
    List<ItemStack> items = sakiewka.getStoredItems(pouch);
    if (items.isEmpty()) {
      player.sendMessage(ChatUtils.color(this.plugin.getMessagesConfig().getString("items.sakiewka_dropu.empty_message", "&cTwoja sakiewka jest pusta!")));
      player.closeInventory();
      return;
    } 
    for (ItemStack item : items) {
      if (item != null && item.getType() != Material.AIR)
        player.getInventory().addItem(new ItemStack[] { item }).values().forEach(remaining -> player.getWorld().dropItemNaturally(player.getLocation(), remaining)); 
    } 
    sakiewka.saveItems(pouch, new ArrayList());
    player.sendMessage(ChatUtils.color(this.plugin.getMessagesConfig().getString("items.sakiewka_dropu.received_message", "&aPomyslnie odebrano przedmioty z sakiewki!")));
    player.closeInventory();
  }
  
  @EventHandler
  public void onSwap(PlayerSwapHandItemsEvent event) {
    if (isForbiddenOffhandItem(event.getMainHandItem()) || isForbiddenOffhandItem(event.getOffHandItem()))
      event.setCancelled(true); 
  }
  
  @EventHandler
  public void onInventoryDrag(InventoryDragEvent event) {
    if (isSakiewka(event.getOldCursor())) {
      if (event.getRawSlots().contains(Integer.valueOf(45))) {
        event.setCancelled(true);
        return;
      } 
      Iterator<Integer> iterator = event.getRawSlots().iterator();
      while (iterator.hasNext()) {
        int slot = ((Integer)iterator.next()).intValue();
        ItemStack target = event.getView().getItem(slot);
        if (target != null && target.getType() != Material.AIR && (isSakiewka(target) || target.isSimilar(event.getOldCursor()))) {
          event.setCancelled(true);
          return;
        } 
      } 
    } 
  }
  
  private boolean isSakiewka(ItemStack item) {
    if (item == null)
      return false; 
    ItemMeta meta = item.getItemMeta();
    return (meta == null) ? false : "sakiewka_dropu".equals(meta.getPersistentDataContainer().get(EVENT_KEY, PersistentDataType.STRING));
  }
  
  private boolean isForbiddenOffhandItem(ItemStack item) {
    if (item == null)
      return false; 
    ItemMeta meta = item.getItemMeta();
    if (meta == null)
      return false; 
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    if (pdc.has(COSTUME_KEY, PersistentDataType.STRING))
      return true; 
    if (pdc.has(PET_KEY, PersistentDataType.STRING))
      return true; 
    if (pdc.has(PARROT_KEY, PersistentDataType.STRING))
      return true; 
    String eid = (String)pdc.get(EVENT_KEY, PersistentDataType.STRING);
    return "sakiewka_dropu".equals(eid);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\listeners\InventoryListener.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */