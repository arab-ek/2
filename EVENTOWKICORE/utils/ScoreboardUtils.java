package dev.arab.EVENTOWKICORE.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardUtils {
  private static final Map<UUID, String> teamNameCache = new HashMap<>();
  
  public static void applyRgbTeam(Player player, ChatColor color) {
    String teamName = getRgbTeamName(player);
    Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
    for (Player online : Bukkit.getOnlinePlayers()) {
      Scoreboard board = online.getScoreboard();
      applyToBoard(board, player, teamName, color);
      if (board != mainBoard)
        applyToBoard(mainBoard, player, teamName, color); 
    } 
    if (Bukkit.getPluginManager().isPluginEnabled("TAB"))
      try {
        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer != null) {
          NameTagManager ntm = TabAPI.getInstance().getNameTagManager();
          if (ntm != null);
        } 
      } catch (Throwable throwable) {} 
  }
  
  private static String getRgbTeamName(Player player) {
    return teamNameCache.computeIfAbsent(player.getUniqueId(), uuid -> {
          String name = player.getName();
          if (name.length() > 10)
            name = name.substring(0, 10); 
          return "00rgb_" + name;
        });
  }
  
  private static void applyToBoard(Scoreboard board, Player player, String teamName, ChatColor color) {
    if (board == null)
      return; 
    Team team = board.getTeam(teamName);
    if (team == null)
      try {
        team = board.registerNewTeam(teamName);
      } catch (Exception e) {
        team = board.getTeam(teamName);
      }  
    if (team != null) {
      if (team.getColor() != color)
        team.setColor(color); 
      if (team.getOption(Team.Option.COLLISION_RULE) != Team.OptionStatus.NEVER)
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER); 
      if (!team.hasEntry(player.getName())) {
        removePlayerFromOtherRgbTeams(board, player, teamName);
        team.addEntry(player.getName());
      } 
    } 
  }
  
  private static void removePlayerFromOtherRgbTeams(Scoreboard board, Player player, String currentTeamName) {
    if (board == null)
      return; 
    Team existing = board.getEntryTeam(player.getName());
    if (existing != null && existing.getName().contains("rgb_") && !existing.getName().equals(currentTeamName))
      existing.removeEntry(player.getName()); 
  }
  
  public static void removeFromRgbTeams(Player player) {
    for (Player online : Bukkit.getOnlinePlayers())
      cleanupPlayerRgb(online.getScoreboard(), player); 
    cleanupPlayerRgb(Bukkit.getScoreboardManager().getMainScoreboard(), player);
    if (Bukkit.getPluginManager().isPluginEnabled("TAB"))
      try {
        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer != null) {
          NameTagManager ntm = TabAPI.getInstance().getNameTagManager();
          if (ntm != null);
        } 
      } catch (Throwable throwable) {} 
  }
  
  private static void cleanupPlayerRgb(Scoreboard board, Player player) {
    if (board == null)
      return; 
    String teamName = getRgbTeamName(player);
    Team team = board.getTeam(teamName);
    if (team != null)
      team.unregister(); 
    for (Team t : board.getTeams()) {
      if (t.getName().contains("rgb_") && t.hasEntry(player.getName()))
        t.removeEntry(player.getName()); 
    } 
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICOR\\utils\ScoreboardUtils.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */