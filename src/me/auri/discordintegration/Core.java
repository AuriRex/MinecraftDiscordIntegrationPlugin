package me.auri.discordintegration;

import java.io.IOException;
import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.*;

import me.auri.discordintegration.commands.*;
import me.auri.discordintegration.enc.EncMode;
import me.auri.discordintegration.enc.EncModeAES;


public class Core {
    
    public static Variables var;

    public static Main main;

    public static FileConfiguration config;

    public static Util util;

	private static HashMap<String, Command> commands = new HashMap<>();

	private static String vars_save_file;

	private static EventSenderThread est;
	private static EventReceiverThread ert;

	public static String getVersion() {
		return "1.2.2";
	}

    protected static void onLoad(Main main) {

        Core.main = main;

        config = main.getConfig();

        util = new Util(main);

		vars_save_file = main.getDataFolder().getAbsolutePath() + "/var_config.yml";

        var = new Variables(vars_save_file);


        var.setDefault("discordintegration.plugin.name", ChatColor.GOLD + "[" + ChatColor.BLUE + "DiscordIntegration" + ChatColor.GOLD + "] " + ChatColor.GREEN);
		
		var.setDefault("discordintegration.chat.prefix", ChatColor.WHITE + "<" + ChatColor.BLUE + "D" + ChatColor.WHITE + "> " + ChatColor.GOLD);
		var.setDefault("discordintegration.chat.suffix", ChatColor.WHITE + " > ");
		var.setDefault("discordintegration.chat.color", ChatColor.GRAY);
		
        var.setDefault("discordintegration.bot.host", "127.0.0.1");
        var.setDefault("discordintegration.bot.port", "11001");
		var.setDefault("discordintegration.bot.syncname", "TestServer");
		var.setDefault("discordintegration.bot.encryption", "AES");
		var.setDefault("discordintegration.bot.enckey", "defaultEncKey");
		
		var.setDefault("discordintegration.debug.enable", false);
		
		//var.setDefault("discordintegration.event.flags.saycommand.preventdoubleevent", true);
		
		var.setDefault("discordintegration.event.chat", true);
		var.setDefault("discordintegration.event.join", true);
		var.setDefault("discordintegration.event.leave", true);
		var.setDefault("discordintegration.event.kick", true);
		var.setDefault("discordintegration.event.shutdown", false);
		var.setDefault("discordintegration.event.command", false);
		var.setDefault("discordintegration.event.death", true);
		var.setDefault("discordintegration.event.teleport", false);
		var.setDefault("discordintegration.event.respawn", false);
		var.setDefault("discordintegration.event.saycommand", true);

		if(config.contains("enable")) {
			if(!config.getBoolean("enable")) {
				System.out.println("DiscordIntegration is disabled in config.yml!");
				return;
			}
		}

		createHelp();

		commands.put("discordintegration", new CmdDiscordIntegration());

		commands.put("debug", new CmdDebug());

		if(isNotSetup()) {
			System.out.println(ChatColor.RED + "##############################");
			System.out.println(var.get("discordintegration.plugin.name") + ChatColor.RED + "Plugin has not been set up yet!");
			System.out.println(ChatColor.RED + "/di var discordintegration.bot.syncname YourServerNameHere");
			System.out.println(ChatColor.RED + "/di var discordintegration.bot.host DiscordBotsIPOrHostnameHere");
			System.out.println(ChatColor.RED + "/di savevar");
			System.out.println(ChatColor.RED + "/di reconnect");
			System.out.println(ChatColor.RED + "##############################");
			return;
		}

		createDIThreads();

	}

	private static EncMode encMode = new EncModeAES();

	public static void createDIThreads() {

		String encMethod = var.get("discordintegration.bot.encryption");

		switch(encMethod) {
			case "AES":
				encMode = new EncModeAES();
				break;
			case "none":
			default:
				encMode = new EncMode();
		}

		est = new EventSenderThread(var.get("discordintegration.bot.host"), Integer.parseInt(var.get("discordintegration.bot.port")), var.get("discordintegration.bot.syncname"), encMode, new String[] {Core.var.get("discordintegration.bot.enckey")});
		ert = new EventReceiverThread(var.get("discordintegration.bot.host"), Integer.parseInt(var.get("discordintegration.bot.port")), var.get("discordintegration.bot.syncname"), encMode, new String[] {Core.var.get("discordintegration.bot.enckey")});

		est.start();
		ert.start();
	}

	private static boolean reconnectAttempt = false;

	public static void sendEvent(String event, String content) {
		if(isNotSetup() || isAboutToDisable) {
			return;
		}
		if (est == null || !est.isConnected()) {
			if (reconnectAttempt)
				return;
			reconnectAttempt = true;
			new Thread() {
                public void run() {

                    int c = 10;
                    System.out.println(ChatColor.RED + "Attempting reconnection in " + c + "Seconds");

                    while(c > 0) {
                        try {
                            
                            Thread.sleep(1000);
                            c -= 1;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    System.out.println(ChatColor.RED + "Attempting reconnection ...");

					Core.reconnectDIThreads();
                }
            }.start();
			return;
		}
		est.sendEvent(event, content);
	}

	public static void onEnable() {

		isAboutToDisable = false;
		config.addDefault("enable", true);
		config.options().copyDefaults(true);

	}

	private static boolean isAboutToDisable = false;
	
	public static void onDisable() {

		isAboutToDisable = true;
		//Core.sendEvent("ServerShutdownEvent", "Plugin Disabled");

		if(est != null) {
			try {
				est.close();
				ert.close();
			} catch(Exception ex) {
	
			}
		}
		
		est = null;
		ert = null;

		saveVars();

	}

	public static boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label,
			String[] args) {
		String name = command.getName().toLowerCase();
		if (commands.containsKey(name)) {
			return commands.get(name).run(sender, command, label, args);
		}
		return false;
	}

	

	private static ArrayList<String[]> help = new ArrayList<String[]>();

	private static void createHelp() {
    	
		String _help = "discordintegration : Main command;"
						+" + help [page] : Display this;"
						+" + var <var> [value] : get / set a var;"
						+" + savevar : save vars to file;"
						+" + reloadvar : reload vars from file;"
						+" + reconnect : try to connect to discord;"
						+" + stopthreads : stops the DiscordIntegration Threads;"
						+"debug : Prints all vars to the console;";
    	
    	
    	int count = 0;
    	int max_count = 6;
    	String[] str_a_b = new String[max_count];
    	for(String str : _help.split(";")) {
    		if(!str.equalsIgnoreCase("")) {
    			
    			String[] _sa = str.split(":", 2);
    			
    			String cmd = _sa[0]; // command name
    			String desc = _sa[1];
    			
    			str_a_b[count] = ChatColor.GOLD + cmd + ChatColor.AQUA + "-" + desc;
    			
    			count++;
    			
    			if(count >= max_count) {
    				count = 0;
    				
    				help.add(str_a_b);
    				
    				str_a_b = new String[max_count];
    			}
			}
		}

		if (count != 0) {
			help.add(str_a_b);
		}
		
	}

    
	public static void sendHelpMsg(CommandSender sender, int page) {
		
		int _page = Math.max(0, Math.min(page-1, help.size()-1));
		
		sender.sendMessage(Core.var.get("discordintegration.plugin.name") + ChatColor.GOLD + " Help List - Page "+(_page+1)+"/"+help.size() + " v" + getVersion());
		
		if(_page >= help.size() || _page < 0) {
			sender.sendMessage("Error providing this page!");
			return;
		}
		String[] _page_x = help.get(_page);
		if(_page_x == null) {
			sender.sendMessage("Error providing this page!");
			return;
		}
		for (String hp : _page_x) {
			if(hp != null)
				sender.sendMessage(hp);
		}
		
		sender.sendMessage("-----------------------------------------------------");
		
	}

	public static void closeDIThreads() {
		if(est != null) {
			est.close();
		}
		if(ert != null) {
			ert.close();
		}
	}

	public static void reconnectDIThreads() {
		if(isAboutToDisable || reconnectAttempt) return;
		reconnectAttempt = true;
		Collection<String> events = null;
		if(est != null) {
			events = est.getEventQueue();
			closeDIThreads();
		}
		createDIThreads();
		if(events != null)
			est.setEventQueue(events);
		reconnectAttempt = false;
	}

	public static boolean isEventEnabled(String e) {
		return Boolean.valueOf(var.get("discordintegration.event."+e));
	}

	public static void loadVars() {
		var = new Variables(vars_save_file);
	}

	public static void saveVars() {
		try {
			var.save(vars_save_file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isDebug() {
		return var.getBool("discordintegration.debug.enable");
	}
	
	/***
	 * returns false if the plugin has been setup
	 * @return
	 */
	public static boolean isNotSetup() {
		return Core.var.get("discordintegration.bot.syncname").equals("TestServer") && !Boolean.valueOf(Core.var.get("discordintegration.bot.testnameoverride"));
	}

}