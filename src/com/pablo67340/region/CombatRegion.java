package com.pablo67340.region;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.pablo67340.region.runnables.LocSave;
import com.pablo67340.region.runnables.RegionZone;
import com.pablo67340.region.utils.Metrics;
import com.pablo67340.region.utils.PlayerListener;
import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;

public class CombatRegion extends JavaPlugin {
	public String prefix = "§7[§cCombatRegion§7]§r";
	public HashMap<String, Location> locsave = new HashMap<String, Location>();
	public HashSet<String> pvp = new HashSet<String>();
	public HashSet<String> spawn = new HashSet<String>();
	public boolean checktag = false;
	public CombatTagApi combatApi;

	public void onEnable(){
		new File("plugins/CombatRegion/").mkdir();
		this.saveDefaultConfig();
		this.startMetrics();
		this.setPrefix();
		this.getServer().getScheduler().runTaskTimer(this, new LocSave(this), 0L, 0L);
		System.out.print("[CombatRegion] Location task started.");
		this.getServer().getScheduler().runTaskTimer(this, new RegionZone(this), 0L, 0L);
		System.out.print("[CombatRegion] Region check task started.");
		if(this.getConfig().getBoolean("pvptag")){
			checkCombatTag();
		}
		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
	}

	private void setPrefix(){
		String prefixfromconfig = this.getConfig().getString("prefix");
		if (prefixfromconfig != null) {
			prefix = ChatColor.translateAlternateColorCodes('&', prefixfromconfig);
		}	
	}

	private void startMetrics(){
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			System.out.print("[CombatRegion] Failed to submit Metrics, notify pablo67340 on Dev Bukkit.");
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(command.getName().equalsIgnoreCase("crreload")){
			if(sender.isOp()){
				this.reloadConfig();
				this.setPrefix();
				if(this.getConfig().getBoolean("pvptag")){
					checkCombatTag();
				}else{
					checktag = false;
				}
				sender.sendMessage(prefix + " §7Reloaded configuration file!");
			}else{
				sender.sendMessage(prefix + " ¤cSorry you don't have permission.");
			}
		}
		return false;
	}

	private void checkCombatTag(){
		if(getServer().getPluginManager().getPlugin("WorldGuard") == null){
			System.out.print("[CombatRegion] WorldGuard not found, disabling.");
			this.getPluginLoader().disablePlugin(this);
		}
		if(getServer().getPluginManager().getPlugin("CombatTag") != null){
			combatApi = new CombatTagApi((CombatTag)getServer().getPluginManager().getPlugin("CombatTag"));
			checktag = true;
		}else{
			System.out.print("[CombatRegion] CombatTag not found, disabling.");
			this.getPluginLoader().disablePlugin(this);
		}
	}

	public void sendAlert(Player p, String alert){
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix) + " " + ChatColor.translateAlternateColorCodes('&', alert));

	}

	public boolean checkWorld(Player p){
		int size = this.getConfig().getStringList("disabledworlds").size();
		int wcheck = size;
		String world = p.getLocation().getWorld().getName();
		for(String disabledworld : this.getConfig().getStringList("disabledworlds")){
			if(disabledworld != null){
				if(world.equalsIgnoreCase(disabledworld)){
					wcheck--;
				}
			}
		}
		if(wcheck == size){
			return true;
		}else{
			wcheck = size;
			return false;
		}
	}

}