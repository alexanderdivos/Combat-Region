package com.pablo67340.region.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.pablo67340.region.CombatRegion;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class PlayerListener implements Listener {
	CombatRegion cr;
	public PlayerListener(CombatRegion combatRegion){
		cr = combatRegion;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		if(cr.getConfig().getBoolean("motd")){
			cr.sendAlert(e.getPlayer(), "§7is used on this server, made by §fpablo67340§7.");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onDeath(PlayerRespawnEvent event){
		cr.pvp.remove(event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDmg(EntityDamageByEntityEvent event){
		if(cr.getConfig().getBoolean("pvpbypass")){
			if(event.getEntity() instanceof Player){
				Player player = (Player) event.getEntity();
				if(event.isCancelled()){
					if(cr.combatApi.isInCombat(player.getName())){
						event.setCancelled(false);
					}
				}
			}
		}else{
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onTeleport(PlayerTeleportEvent event){
		Location loc = event.getTo();
		RegionManager regions = WGBukkit.getRegionManager(loc.getWorld());
		for (String rname : cr.getConfig().getStringList("regions")) {
			ProtectedRegion region = regions.getRegion(rname);
			if(region != null){
				if(cr.pvp.contains(event.getPlayer().getName())){
					if(ProtectedRegion.isValidId(region.getId())){
						if(region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
							cr.pvp.remove(event.getPlayer().getName());
						}
					}else{
						if (Cooldowns.tryCooldown("CR", "notifydelay", 12000L)) {
							System.out.print("[CombatRegion] Region " + region.getId() + " is invalid.");
						}
					}
				}
			}else{
				return;
			}
		}
	}

}
