package me.auri.discordintegration;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class Util {

	private final Main main;
	
	public Util(Main main) {
		this.main = main;
	}
	
	public void broadcastTitle(String title, String subtitle, int fadein, int stay, int fadeout) {
		for(Player ply : main.getServer().getOnlinePlayers()) {
			ply.sendTitle(title, subtitle, fadein, stay, fadeout);
		}
	}
	
	public boolean isDay(World wrl) {
	    long time = wrl.getTime();
	    return time < 12300 || time > 23850;
	}
	
	public long dncCalc(World wrl) {
		long time = wrl.getTime();
		
		long all = 24000;
		long day_time = 12450;
		long new_time = time;
		
		if(time >= 23850) {
			new_time = time - 23850;
		} else if (time >= 12300) {
			new_time = time - 12300;
		}
		
		if(isDay(wrl)) {
			new_time = (new_time - day_time) * -1;
		} else {
			new_time = (new_time - (all - day_time)) * -1;
		}
		
		return new_time;
	}
	
	public String dncFancy(long time) {
		
		long seconds = (long) (((double) time / 1000.0) * 50.0);
	
		int minutes = (int) ( (double) seconds / 60.0);
		
		int rem_secs = (int) ( (double) seconds % 60.0 );
		
		return minutes+"m "+rem_secs+"s";
	}
	
	public String dncCompl(World wrl) {
		return dncFancy(dncCalc(wrl));
	}
	
}
