package me.auri.discordintegration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	@Override
	public void onLoad() {
		
		Core.onLoad(this);

	}
	
	@Override
    public void onEnable() {
		System.out.println("[DiscordIntegration] Plugin enabled.");
		
		//createHelp();
		
		Core.onEnable();

	    saveConfig();
	    
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		
    }
	
    @Override
    public void onDisable() {
		
		Core.onDisable();
    	
    	saveConfig();
    	
    	System.out.println("[DiscordIntegration] Plugin disabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
		
		return Core.onCommand(sender, command, label, args);

    }
	
    
}
