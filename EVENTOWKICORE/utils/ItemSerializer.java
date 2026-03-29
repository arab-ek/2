package dev.arab.EVENTOWKICORE.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class ItemSerializer {
  public static byte[] serializeItemsToBytes(List<ItemStack> items) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      GZIPOutputStream gzipOutput = new GZIPOutputStream(outputStream);
      BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(gzipOutput);
      dataOutput.writeInt(items.size());
      for (ItemStack item : items)
        dataOutput.writeObject(item); 
      dataOutput.close();
      gzipOutput.close();
      return outputStream.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
      return new byte[0];
    } 
  }
  
  public static List<ItemStack> deserializeItemsFromBytes(byte[] data) {
    if (data == null || data.length == 0)
      return new ArrayList<>(); 
    try {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
      GZIPInputStream gzipInput = new GZIPInputStream(inputStream);
      BukkitObjectInputStream dataInput = new BukkitObjectInputStream(gzipInput);
      int size = dataInput.readInt();
      List<ItemStack> items = new ArrayList<>();
      for (int i = 0; i < size; i++)
        items.add((ItemStack)dataInput.readObject()); 
      dataInput.close();
      gzipInput.close();
      return items;
    } catch (Exception e) {
      return new ArrayList<>();
    } 
  }
  
  public static String serializeItems(List<ItemStack> items) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
      dataOutput.writeInt(items.size());
      for (ItemStack item : items)
        dataOutput.writeObject(item); 
      dataOutput.close();
      return Base64Coder.encodeLines(outputStream.toByteArray());
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    } 
  }
  
  public static List<ItemStack> deserializeItems(String data) {
    try {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
      BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
      int size = dataInput.readInt();
      List<ItemStack> items = new ArrayList<>();
      for (int i = 0; i < size; i++)
        items.add((ItemStack)dataInput.readObject()); 
      dataInput.close();
      return items;
    } catch (Exception e) {
      return new ArrayList<>();
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICOR\\utils\ItemSerializer.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */