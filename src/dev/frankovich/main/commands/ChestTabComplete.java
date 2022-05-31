package dev.frankovich.main.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import dev.frankovich.main.Utils;

public class ChestTabComplete implements TabCompleter 
{
    private static final List<String> COMMANDS = Arrays.asList("add", "del", "list", "ledger");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) 
    {
        if (sender instanceof Player)
        {
            if (args.length == 1)
            {
                return COMMANDS;
            }
            /*
            else if (args.length == 2) 
            {
                List<String> idsofplayer = Utils.getInteractableIds(sender.getName()); 
                return idsofplayer;
            }
            */
        }
        
        return null;
    }

}
