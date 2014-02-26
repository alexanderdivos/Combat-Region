package com.pablo67340.region.runnables;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.pablo67340.region.CombatRegion;
import com.pablo67340.region.utils.Cooldowns;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionZone extends BukkitRunnable {
	CombatRegion cr;
	public RegionZone(CombatRegion combatRegion){
		cr = combatRegion;
	}

	@Override
	public void run() {
		if(cr.checktag){
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if(!p.hasPermission("combatregion.bypass")){
					if(cr.checkWorld(p)){
						if(cr.combatApi.isInCombat(p.getName())){
							checkEntry(p);
						}
					}
				}
			}
		}else{
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if(!p.hasPermission("combatregion.bypass")){
					if(cr.checkWorld(p)){
						checkEntry(p);
					}
				}
			}
		}
	}

	private void checkEntry(Player p){
		Location loc = p.getLocation();
		RegionManager regions = WGBukkit.getRegionManager(loc.getWorld());
		for (String rname : cr.getConfig().getStringList("regions")) {
			ProtectedRegion region = regions.getRegion(rname);
			if(region != null){
				if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
					denyEntry(p);
				}
			}else{
				return;
			}
		}
	}

	private void denyEntry(Player p) {
		String pname = p.getName();
		if (cr.pvp.contains(pname)) {
			Location savedlocation = cr.locsave.get(pname);
			p.teleport(savedlocation);
			tryAlert(p);
		}
	}

	private void tryAlert(Player p) {
		String pname = p.getName();
		if (Cooldowns.tryCooldown(pname, "kbcool", 6000L)) {
			String msg = cr.getConfig().getString("msg");
			if (msg != null){ 
				p.sendMessage(cr.prefix + " " + ChatColor.translateAlternateColorCodes('&', msg));	
			}else{
				p.sendMessage(cr.prefix + " §7You can't run away from pvp! Type §f/spawn§7.");	
			}
		}			
	}

}
