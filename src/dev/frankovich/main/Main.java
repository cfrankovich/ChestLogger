package dev.frankovich.main;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import dev.frankovich.main.commands.*;
import dev.frankovich.main.listener.ChestListener;

public class Main extends JavaPlugin 
{
	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		new ChestCommand(this);
		this.getCommand("chest").setTabCompleter(new ChestTabComplete());
		new ChestListener(this);
		new File("./plugins/ChestLogger/Chests/").mkdir();
		new File("./plugins/ChestLogger/Ledgers/").mkdir();
		new Utils(this);
		Bukkit.getLogger().info("[ChestLogger] Plugin enabled!");
	}
}
