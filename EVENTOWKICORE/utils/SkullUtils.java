package dev.arab.EVENTOWKICORE.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullUtils {
  private static Class<?> gameProfileClass;
  
  private static Constructor<?> gameProfileConstructor;
  
  private static Class<?> propertyClass;
  
  private static Class<?> propMapClass;
  
  private static Constructor<?> propertyConstructor;
  
  private static Method getPropertiesMethod;
  
  private static Method putMethod;
  
  private static Field profileField;
  
  public static ItemStack getCustomSkull(String value) {
    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
    if (value == null || value.isEmpty())
      return item; 
    SkullMeta meta = (SkullMeta)item.getItemMeta();
    if (meta == null)
      return item; 
    mutateItemMeta(meta, value);
    item.setItemMeta((ItemMeta)meta);
    return item;
  }
  
  public static void applySkin(ItemStack item, String value) {
    if (item == null || item.getType() != Material.PLAYER_HEAD || value == null || value.isEmpty())
      return; 
    SkullMeta meta = (SkullMeta)item.getItemMeta();
    if (meta == null)
      return; 
    mutateItemMeta(meta, value);
    item.setItemMeta((ItemMeta)meta);
  }
  
  private static void mutateItemMeta(SkullMeta meta, String value) {
    try {
      if (gameProfileConstructor == null || propertyConstructor == null)
        return; 
      if (profileField == null) {
        profileField = findField(meta.getClass(), "profile");
        if (profileField != null)
          profileField.setAccessible(true); 
      } 
      if (profileField == null)
        return; 
      Object profile = gameProfileConstructor.newInstance(new Object[] { UUID.randomUUID(), "" });
      Object property = propertyConstructor.newInstance(new Object[] { "textures", value });
      Object propertyMap = getPropertiesMethod.invoke(profile, new Object[0]);
      if (putMethod == null)
        putMethod = propertyMap.getClass().getMethod("put", new Class[] { Object.class, Object.class }); 
      putMethod.invoke(propertyMap, new Object[] { "textures", property });
      Class<?> fieldType = profileField.getType();
      if (fieldType.isAssignableFrom(gameProfileClass)) {
        profileField.set(meta, profile);
      } else {
        try {
          Constructor<?> constructor = fieldType.getConstructor(new Class[] { gameProfileClass });
          profileField.set(meta, constructor.newInstance(new Object[] { profile }));
        } catch (NoSuchMethodException e) {
          profileField.set(meta, profile);
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  private static Field findField(Class<?> clazz, String fieldName) {
    while (clazz != null && clazz != Object.class) {
      try {
        return clazz.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      } 
    } 
    return null;
  }
  
  static {
    try {
      gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
      gameProfileConstructor = gameProfileClass.getConstructor(new Class[] { UUID.class, String.class });
      propertyClass = Class.forName("com.mojang.authlib.properties.Property");
      propertyConstructor = propertyClass.getConstructor(new Class[] { String.class, String.class });
      getPropertiesMethod = gameProfileClass.getMethod("getProperties", new Class[0]);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICOR\\utils\SkullUtils.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */