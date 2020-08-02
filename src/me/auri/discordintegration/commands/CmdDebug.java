package me.auri.discordintegration.commands;

import org.bukkit.command.CommandSender;

import me.auri.discordintegration.Core;

public class CmdDebug implements Command {

    @Override
    public boolean run(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        
        Core.var.debug();

        return true;
    }
    
}