package dev.arab.EVENTOWKICORE.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import dev.arab.Main;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BlockTracker {
  private final File file;
  
  private FileConfiguration config;
  
  private final Set<String> placedBlocks = new HashSet<>();
  
  public BlockTracker(Main plugin) {
    this.file = new File(plugin.getDataFolder(), "blocks.yml");
    load();
  }
  
  public void load() {
    if (!this.file.exists())
      try {
        this.file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }  
    this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(this.file);
    List<String> list = this.config.getStringList("placed_bedrock");
    this.placedBlocks.clear();
    this.placedBlocks.addAll(list);
  }
  
  public void save() {
    this.config.set("placed_bedrock", this.placedBlocks.stream().collect(Collectors.toList()));
    try {
      this.config.save(this.file);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void addBlock(Location loc) {
    this.placedBlocks.add(serialize(loc));
    save();
  }
  
  public boolean isPlacedBlock(Location loc) {
    return this.placedBlocks.contains(serialize(loc));
  }
  
  public void removeBlock(Location loc) {
    this.placedBlocks.remove(serialize(loc));
    save();
  }
  
  private String serialize(Location loc) {
    return loc.getWorld().getName() + "," + loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY();
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICOR\\utils\BlockTracker.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */