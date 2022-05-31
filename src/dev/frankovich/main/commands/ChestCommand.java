package dev.frankovich.main.commands;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.frankovich.main.Main;
import dev.frankovich.main.Utils;

public class ChestCommand implements CommandExecutor 
{
	private Main plugin;
	
	public ChestCommand(Main plugin)
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
			sender.sendMessage("§c[ChestLogger] You do not have permission to execute this command.");
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
			else if (arg.equals("list"))
			{
				list(p);
				return true;
			}
			else if (arg.equals("ledger"))
			{
				ledger(p, args);
				return true;
			}
			else if (arg.equals("clear"))
			{
				clearledger(p, args);
				return true;
			}

		}
		
		return false;
	}

	/* Add an entry to the "data base"
	 * @param p - Player in question
	 * @param args - arguments passed to command 
	*/
	private void add(Player p, String[] args)
	{
		Block b = p.getTargetBlockExact(4);

		/* If nothing is found just return */
		if (b == null)
		{
			p.sendMessage("§c[ChestLogger] No blocks found or in range!");
			return;
		}
		
		/* Verify that the block is a chest */
		if (!b.getType().equals(Material.CHEST))
		{
			p.sendMessage("§c[ChestLogger] This is not a chest! Please aim at a chest to run this command.");
			return;
		}

		Chest ch = (Chest) b.getLocation().getBlock().getState();
		ItemStack[] stack = ch.getInventory().getContents();
		if (Utils.chestBeingWatched(p, b))
		{
			p.sendMessage("§c[ChestLogger] You are already watching this chest!");	
			return;
		}
		Utils.newChestEntry(p.getName(), p.getUniqueId().toString(), stack, b.getLocation().getBlockX(), b.getLocation().getBlockY(), b.getLocation().getBlockZ(), false);

	}

	/* Remove an entry from the "data base"
	 * @param p - Player requesting the delete 
	 * @param args - arguments passed to command 
	*/
	private void del(Player p, String[] args)
	{
		try
		{
            String idstr = String.format("%06d", Integer.parseInt(args[1]));
			Utils.deleteChest(p, idstr);
		}
		catch (IndexOutOfBoundsException e)
		{
			p.sendMessage("§c[ChestLogger] Please provide an id");
		}
		p.sendMessage("[§dChestLogger§f] Removed chest from your watchlist");
	}

	/* Lists all the chests the player is watching
	 * @param p - Player requesting the list 
	*/
	private void list(Player p)
	{
		p.sendMessage(" ");
		p.sendMessage("§l[§d§lWatched Chests§f§l]"); 
		Utils.sendWatchedChests(p);
	}

	/* Sends the chest ledger to player given from id
	 * @param p - player requesting ledger
	 * @param args - arguments given
	*/
	private void ledger(Player p, String[] args)
	{
		String idstr;
		try
		{
            idstr = String.format("%06d", Integer.parseInt(args[1]));
			try
			{	
				Utils.printLedger(p, idstr);
			}
			catch (IOException e)
			{
				Bukkit.getLogger().info("[ChestLogger] Error with reading ledger file");
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			p.sendMessage("§c[ChestLogger] Please provide an id");
		}
	} 

	private void clearledger(Player p, String[] args)
	{
		String idstr;
		try
		{
            idstr = String.format("%06d", Integer.parseInt(args[1]));
			try
			{	
				Utils.clearLedger(p, idstr);
			}
			catch (IOException e)
			{
				Bukkit.getLogger().info("[ChestLogger] Error with reading ledger file");
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			p.sendMessage("§c[ChestLogger] Please provide an id");
		}

		p.sendMessage("[§dChestLogger§f] Ledger cleared"); 
	} 
}
