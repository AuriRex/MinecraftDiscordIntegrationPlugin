package me.auri.discordintegration.commands;

import org.bukkit.command.CommandSender;

public interface Command {
    
    public boolean run(CommandSender sender, org.bukkit.command.Command command, String label, String[] args);

}