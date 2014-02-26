package com.pablo67340.region.runnables;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.pablo67340.region.CombatRegion;
import com.pablo67340.region.utils.Cooldowns;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class LocSave extends BukkitRunnable {
	CombatRegion cr;
	public LocSave(CombatRegion combatRegion){
		cr = combatRegion;
	}

	@Override
	public void run() {
		if(cr.checktag){
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if(!p.hasPermission("combatregion.bypass")){
					if(cr.checkWorld(p)){
						if(cr.combatApi.isInCombat(p.getName())){
							check(p);
						}else{
							return;
						}
					}
				}
			}
		}else{
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if(!p.hasPermission("combatregion.bypass")){
					if(cr.checkWorld(p)){
						check(p);
					}
				}
			}
		}
	}

	public void check(Player p){
		int rcheck = 0;
		String pname = p.getName();
		Location loc = p.getLocation();
		RegionManager regions = WGBukkit.getRegionManager(loc.getWorld());
		for (String rname : cr.getConfig().getStringList("regions")) {
			ProtectedRegion region = regions.getRegion(rname);
			if (region != null) {
				if(ProtectedRegion.isValidId(region.getId())){
					if (!region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
						rcheck++;
					}else{
						return;
					}
				}else{
					if (Cooldowns.tryCooldown("CR", "notifydelay", 12000L)) {
						System.out.print("[CombatRegion] Region " + region.getId() + " is invalid.");
					}
				}
			}else{
				if (Cooldowns.tryCooldown("CR", "notifydelay", 12000L)) {
					System.out.print("[CombatRegion] One or more region names in config do not exist.");
				}
			}
		}
		if(rcheck == cr.getConfig().getStringList("regions").size()){
			cr.locsave.put(pname, p.getLocation());
			cr.pvp.add(pname);
			rcheck = 0;
		}
	}

}
