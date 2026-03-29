package dev.arab.EVENTOWKICORE.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {
  private final boolean enabled;
  
  private RegionQuery cachedQuery;
  
  public WorldGuardHook(boolean enabled) {
    this.enabled = enabled;
    if (enabled)
      try {
        this.cachedQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
      } catch (Exception exception) {} 
  }
  
  private RegionQuery getQuery() {
    if (this.cachedQuery == null && this.enabled)
      this.cachedQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery(); 
    return this.cachedQuery;
  }
  
  public boolean isPlayerInBlockedRegion(Player player, List<String> blockedRegions) {
    if (!this.enabled || blockedRegions == null || blockedRegions.isEmpty())
      return false; 
    RegionQuery query = getQuery();
    if (query == null)
      return false; 
    ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
    for (ProtectedRegion region : set) {
      if (blockedRegions.contains(region.getId()))
        return true; 
    } 
    return false;
  }
  
  public boolean isLocationInBlockedRegion(Location location, List<String> blockedRegions) {
    if (!this.enabled || blockedRegions == null || blockedRegions.isEmpty())
      return false; 
    try {
      RegionQuery query = getQuery();
      if (query == null)
        return false; 
      ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
      for (ProtectedRegion region : set) {
        if (blockedRegions.contains(region.getId()))
          return true; 
      } 
    } catch (Exception e) {
      return false;
    } 
    return false;
  }
  
  public boolean isLocationInAnyRegion(Location location) {
    if (!this.enabled)
      return false; 
    try {
      RegionQuery query = getQuery();
      if (query == null)
        return false; 
      ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
      return (set.size() > 0);
    } catch (Exception e) {
      return false;
    } 
  }
  
  public Set<String> getRegionIdsAtLocation(Location location) {
    Set<String> ids = new HashSet<>();
    if (!this.enabled)
      return ids; 
    try {
      RegionQuery query = getQuery();
      if (query == null)
        return ids; 
      ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
      for (ProtectedRegion region : set)
        ids.add(region.getId()); 
    } catch (Exception exception) {}
    return ids;
  }
  
  public boolean canBuild(Player player, Location location) {
    if (!this.enabled)
      return true; 
    try {
      RegionQuery query = getQuery();
      return (query == null) ? true : query.testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), new StateFlag[] { Flags.BUILD });
    } catch (Exception e) {
      return true;
    } 
  }
  
  public boolean isEnabled() {
    return this.enabled;
  }
}


/* Location:              C:\Users\bosiwo\Desktop\MINECRAFT\coreeveszafaitp.jar!\loluszek\pl\paczkiAnaeventowki\EVENTOWKICORE\hooks\WorldGuardHook.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */