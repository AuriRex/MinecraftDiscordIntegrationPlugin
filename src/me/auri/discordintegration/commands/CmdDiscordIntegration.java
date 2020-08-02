package me.auri.discordintegration.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.auri.discordintegration.Core;

public class CmdDiscordIntegration implements Command {

    @Override
    public boolean run(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        
        

        if(args.length > 0) {

            if(args[0].equalsIgnoreCase("help")) {
                int page = 0;
                if(args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch(Exception ex) {
                        page = 0;
                    }
                }
                Core.sendHelpMsg(sender, page);
            }

            if(args[0].equalsIgnoreCase("var")) {

                if(args.length > 1) {

                    if(args[1].equals("")) {
                        return true;
                    }

                    if(Core.var.get(args[1]) != null) {

                        if(args.length > 2) {

                            if(args[2].equals("")) {
                                return true;
                            }

                            String prev = Core.var.get(args[1]);

                            Core.var.set(args[1], ChatColor.translateAlternateColorCodes('&', args[2]));

                            System.out.println(Core.var.get("discordintegration.plugin.name") + " Var \""+args[1]+"\" changed: " + prev + " -> " + ChatColor.translateAlternateColorCodes('&', args[2]));

                            sender.sendMessage(Core.var.get("discordintegration.plugin.name") + "+ \""+args[1]+"\": " + Core.var.get(args[1]));
                        } else {
                            sender.sendMessage(Core.var.get("discordintegration.plugin.name") + "> \""+args[1]+"\": " + Core.var.get(args[1]));
                        }

                        return true;


                    } else {
                        sender.sendMessage(Core.var.get("discordintegration.plugin.name") + "var \""+args[1]+"\" does not exist!");
                        return true;
                    }


                }

            }

            if(args[0].equalsIgnoreCase("savevar")) {
                Core.saveVars();
                sender.sendMessage(Core.var.get("discordintegration.plugin.name") + "vars have been saved!");
            }

            if(args[0].equalsIgnoreCase("loadvar")) {
                Core.loadVars();
                sender.sendMessage(Core.var.get("discordintegration.plugin.name") + "vars have been reloaded!");
            }

            if(args[0].equalsIgnoreCase("reconnect")) {
                Core.reconnectDIThreads();
                sender.sendMessage(Core.var.get("discordintegration.plugin.name") + "reconnect attempted! Check Console for more info.");
            }

        } else {
            if(Core.isNotSetup()) {
                sender.sendMessage(ChatColor.RED + "##############################");
                sender.sendMessage(Core.var.get("discordintegration.plugin.name") + ChatColor.RED + "Plugin has not been set up yet!");
                sender.sendMessage(ChatColor.RED + "/di var discordintegration.bot.syncname YourServerNameHere");
                sender.sendMessage(ChatColor.RED + "/di var discordintegration.bot.host DiscordBotsIPOrHostnameHere");
                sender.sendMessage(ChatColor.RED + "/di savevar");
                sender.sendMessage(ChatColor.RED + "/di reconnect");
                sender.sendMessage(ChatColor.RED + "##############################");
            } else {
                Core.sendHelpMsg(sender, 1);
            }
        }

        return true;
    }
    
}