package dev.frankovich.main;

import org.bukkit.plugin.java.JavaPlugin;

import dev.frankovich.main.commands.*;
import dev.frankovich.main.listener.ChestListener;

public class Main extends JavaPlugin 
{
	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		new Chest(this);
		this.getCommand("chest").setTabCompleter(new ChestTabComplete());
		new ChestListener(this);
	}
}
