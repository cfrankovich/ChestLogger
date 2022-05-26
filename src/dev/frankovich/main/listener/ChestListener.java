package dev.frankovich.main.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import dev.frankovich.main.Main;

public class ChestListener implements Listener 
{
    @SuppressWarnings("unused")
    private Main plugin;

    public ChestListener(Main plugin)
    {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void playerOpenChest(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            if(event.getClickedBlock().getType().equals(Material.CHEST))
            {
                /* some player opened a chest */
            }
        }
    }

}
