package dev.arab.EVENTOWKICORE.tasks;

import java.util.Map;
import dev.arab.EVENTOWKICORE.eventowki.EventItem;
import dev.arab.EVENTOWKICORE.utils.EquipmentCacheManager;
import dev.arab.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PassiveEffectTask extends BukkitRunnable {
  private final Main plugin;
  
  private final NamespacedKey itemKey;
  
  private int kuLimitRegen;
  
  private int kuLimitResist;
  
  private int koronaStrength;
  
  private int koronaSpeed;
  
  private int koronaFire;
  
  private int koronaResist;
  
  private int koronaLuck;
  
  private int ku26Regen;
  
  private int ku26Resist;
  
  private double ku26ExtraHealth;
  
  private PotionEffect peLizak;
  
  public PassiveEffectTask(Main plugin) {
    this.plugin = plugin;
    this.itemKey = new NamespacedKey((Plugin)plugin, "event_item_id");
    cacheValues();
  }
  
  private void cacheValues() {
    this.kuLimitRegen = this.plugin.getConfig().getInt("meta.roza_kupidyna.regen_level", 1);
    this.kuLimitResist = this.plugin.getConfig().getInt("meta.roza_kupidyna.resistance_level", 1);
    this.koronaStrength = this.plugin.getConfig().getInt("meta.korona_anarchii.strength_level", 2);
    this.koronaSpeed = this.plugin.getConfig().getInt("meta.korona_anarchii.speed_level", 2);
    this.koronaFire = this.plugin.getConfig().getInt("meta.korona_anarchii.fire_resistance_level", 1);
    this.koronaResist = this.plugin.getConfig().getInt("meta.korona_anarchii.resistance_level", 3);
    this.koronaLuck = this.plugin.getConfig().getInt("meta.korona_anarchii.luck_level", 1);
    this.ku26Regen = this.plugin.getConfig().getInt("meta.roza_kupidyna_2026.regen_level", 2);
    this.ku26Resist = this.plugin.getConfig().getInt("meta.roza_kupidyna_2026.resistance_level", 1);
    this.ku26ExtraHealth = this.plugin.getConfig().getDouble("meta.roza_kupidyna_2026.extra_health", 10.0D);
    this.peLizak = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0);
  }
  
  public void run() {
    if (Bukkit.getCurrentTick() % 100 == 0)
      cacheValues(); 
    EquipmentCacheManager cache = this.plugin.getEquipmentCacheManager();
    for (Player player : Bukkit.getOnlinePlayers()) {
      EquipmentCacheManager.PlayerEquipment equipment = cache.getEquipment(player.getUniqueId());
      boolean hasRose2026 = false;
      if (equipment != null)
        for (Map.Entry<Integer, EventItem> entry : (Iterable<Map.Entry<Integer, EventItem>>)equipment.slotEventItems.entrySet()) {
          String id = ((EventItem)entry.getValue()).getId();
          apply(player, id, (((Integer)entry.getKey()).intValue() == 39));
          if ("roza_kupidyna_2026".equals(id))
            hasRose2026 = true; 
        }  
      if (!hasRose2026) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null && attr.getBaseValue() != 20.0D)
          attr.setBaseValue(20.0D); 
      } 
    } 
  }
  
  private void apply(Player player, String id, boolean isHelmet) {
    double targetHealth;
    AttributeInstance healthAttr;
    switch (id) {
      case "roza_kupidyna":
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, this.kuLimitRegen - 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, this.kuLimitResist - 1));
        break;
      case "lizak":
        player.addPotionEffect(this.peLizak);
        break;
      case "korona_anarchii":
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, this.koronaStrength - 1));
        if (isHelmet) {
          player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, this.koronaSpeed - 1));
          player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, this.koronaFire - 1));
          player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, this.koronaResist - 1));
          player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 100, this.koronaLuck - 1));
        } 
        break;
      case "roza_kupidyna_2026":
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, this.ku26Regen - 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, this.ku26Resist - 1));
        targetHealth = 20.0D + this.ku26ExtraHealth;
        healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null && healthAttr.getBaseValue() != targetHealth)
          healthAttr.setBaseValue(targetHealth); 
        break;
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\tasks\PassiveEffectTask.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */