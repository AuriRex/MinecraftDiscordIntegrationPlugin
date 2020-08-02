package me.auri.discordintegration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class EventListener implements Listener {


	
	public EventListener() {

	}
	
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		if(Core.isEventEnabled("death"))
			Core.sendEvent("PlayerDeathEvent", event.getDeathMessage());
		
    }
	
	
	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {

		if(Core.isEventEnabled("command"))
			Core.sendEvent("PlayerCommandEvent", event.getPlayer().getName() + ";" + event.getMessage());
		
    }
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
		if(Core.isEventEnabled("join"))
			Core.sendEvent("PlayerJoinEvent", event.getPlayer().getName());
		
    }
	
	
	
	// PlayerLoginEvent
	
	@EventHandler
	public void onServerListPingEvent(ServerListPingEvent event) {

	}
	
	
	@EventHandler
	public void onJoin(PlayerLoginEvent event){
		//Core.sendEvent("PlayerLoginEvent", event.getPlayer().getName());
	}
	
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		
		if(Core.isEventEnabled("chat"))
			Core.sendEvent("PlayerChatEvent", event.getPlayer().getName() + ";" + event.getMessage());
		
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		
		
		
	}
	
	
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		
		
		
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if(Core.isEventEnabled("kick"))
			Core.sendEvent("PlayerKickEvent", event.getPlayer().getName() + ";" + event.getReason());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if(Core.isEventEnabled("leave"))
			Core.sendEvent("PlayerQuitEvent", event.getPlayer().getName());
	}
	
	
}
