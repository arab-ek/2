package dev.arab.ADDONS.INCOGNITO;

import dev.arab.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class IncognitoExpansion extends PlaceholderExpansion {
  private final Main plugin;
  
  private final IncognitoManager incognitoManager;
  
  public IncognitoExpansion(Main plugin, IncognitoManager incognitoManager) {
    this.plugin = plugin;
    this.incognitoManager = incognitoManager;
  }
  
  @NotNull
  public String getIdentifier() {
    return "paczki";
  }
  
  @NotNull
  public String getAuthor() {
    return "loluszek";
  }
  
  @NotNull
  public String getVersion() {
    return "1.0.0";
  }
  
  public boolean persist() {
    return true;
  }
  
  public String onRequest(OfflinePlayer player, @NotNull String params) {
    if (player == null)
      return ""; 
    boolean isIncognito = this.incognitoManager.isIncognito(player.getUniqueId());
    return params.equalsIgnoreCase("clan") ? (isIncognito ? "????" : "") : (params.equalsIgnoreCase("nick") ? (isIncognito ? this.incognitoManager.getIncognitoNick(player.getUniqueId()) : ((player.getName() != null) ? player.getName() : "")) : null);
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\INCOGNITO\IncognitoExpansion.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */