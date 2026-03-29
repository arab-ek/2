package dev.arab.EVENTOWKICORE.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class CooldownManager {
  private final Map<UUID, Map<String, Long>> playerCooldowns = new ConcurrentHashMap<>();
  
  public void setCooldown(UUID playerUUID, String itemId, int seconds) {
    if (seconds <= 0)
      return; 
    ((Map<String, Long>)this.playerCooldowns.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>())).put(itemId, Long.valueOf(System.currentTimeMillis() + seconds * 1000L));
  }
  
  public boolean hasCooldown(UUID playerUUID, String itemId) {
    Map<String, Long> cooldowns = this.playerCooldowns.get(playerUUID);
    if (cooldowns == null)
      return false; 
    Long endTime = cooldowns.get(itemId);
    if (endTime == null)
      return false; 
    if (endTime.longValue() <= System.currentTimeMillis()) {
      cooldowns.remove(itemId);
      if (cooldowns.isEmpty())
        this.playerCooldowns.remove(playerUUID, cooldowns); 
      return false;
    } 
    return true;
  }
  
  public long getRemainingMs(UUID playerUUID, String itemId) {
    Map<String, Long> cooldowns = this.playerCooldowns.get(playerUUID);
    if (cooldowns == null)
      return 0L; 
    Long endTime = cooldowns.get(itemId);
    if (endTime == null)
      return 0L; 
    long remaining = endTime.longValue() - System.currentTimeMillis();
    return Math.max(0L, remaining);
  }
  
  public void forEachActiveCooldown(UUID playerUUID, BiConsumer<String, Long> action) {
    Map<String, Long> cooldowns = this.playerCooldowns.get(playerUUID);
    if (cooldowns == null || cooldowns.isEmpty())
      return; 
    long now = System.currentTimeMillis();
    Iterator<Map.Entry<String, Long>> it = cooldowns.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Long> entry = it.next();
      long remaining = ((Long)entry.getValue()).longValue() - now;
      if (remaining > 0L) {
        action.accept(entry.getKey(), Long.valueOf(remaining));
        continue;
      } 
      it.remove();
    } 
    if (cooldowns.isEmpty())
      this.playerCooldowns.remove(playerUUID, cooldowns); 
  }
  
  public List<String> getActiveCooldowns(UUID playerUUID) {
    Map<String, Long> cooldowns = this.playerCooldowns.get(playerUUID);
    if (cooldowns == null || cooldowns.isEmpty())
      return Collections.emptyList(); 
    List<String> active = new ArrayList<>();
    long now = System.currentTimeMillis();
    for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
      if (((Long)entry.getValue()).longValue() > now) {
        active.add(entry.getKey());
        continue;
      } 
      cooldowns.remove(entry.getKey());
    } 
    if (cooldowns.isEmpty())
      this.playerCooldowns.remove(playerUUID, cooldowns); 
    return active;
  }
  
  public void removeCooldown(UUID playerUUID, String itemId) {
    Map<String, Long> cooldowns = this.playerCooldowns.get(playerUUID);
    if (cooldowns != null) {
      cooldowns.remove(itemId);
      if (cooldowns.isEmpty())
        this.playerCooldowns.remove(playerUUID, cooldowns); 
    } 
  }
  
  public void clearAllCooldowns(UUID playerUUID) {
    this.playerCooldowns.remove(playerUUID);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICOR\\utils\CooldownManager.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */