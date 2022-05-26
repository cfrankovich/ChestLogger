package dev.frankovich.main.commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.frankovich.main.Main;
import dev.frankovich.main.Utils;

public class Chest implements CommandExecutor 
{
	@SuppressWarnings("unused")
	private Main plugin;
	
	public Chest(Main plugin)
	{
		this.plugin = plugin;
		plugin.getCommand("chest").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		/* Only players can execute this command */
		if (!(sender instanceof Player)) 
		{ 
			sender.sendMessage("[ChestLogger] Only players can execute this command.");
			return true; 
		}

		Player p = (Player) sender;
		if (!p.hasPermission("chest.use"))
		{
			sender.sendMessage("[§dChestLogger§f] You do not have permission to execute this command.");
			return true;
		}

		for (String arg: args)
		{
			if (arg.equals("add"))
			{
				add(p, args);
				return true;
			}
			else if (arg.equals("del"))
			{
				del(p, args);
				return true;
			}

		}
		
		return false;
	}

	/* Add an entry to the "data base"
	 * @param p - Player in question
	 * @param args - arguments passed to command 
	*/
	private int add(Player p, String[] args)
	{
		Block b = p.getTargetBlockExact(4);

		/* If nothing is found just return */
		if (b == null)
		{
			p.sendMessage("[§dChestLogger§f] No blocks found or in range!");
			return 1;
		}
		
		/* Verify that the block is a chest */
		if (!b.getType().equals(Material.CHEST))
		{
			p.sendMessage("[§dChestLogger§f] This is not a chest! Please aim at a chest to run this command.");
			return 1;
		}

		/* Testing purposes */
		String[] i = {"dirt", "diamonds", "grass"};
		int[] w = {23, 42, 59};
		Utils.newChestEntry(plugin, "topfloorboss", "something", i, w, (int) p.getLocation().getX(), (int) p.getLocation().getY(), (int) p.getLocation().getZ());

		p.sendMessage("[§dChestLogger§f] Chest added to your watch list.");

		return 0;
	}

	/* Remove an entry from the "data base"
	 * @param p - Player in question
	 * @param args - arguments passed to command 
	*/
	private int del(Player p, String[] args)
	{
		p.sendMessage("deleting!");
		return 0;
	}

}
