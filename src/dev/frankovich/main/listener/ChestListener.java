package dev.frankovich.main.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import dev.frankovich.main.Main;
import dev.frankovich.main.Utils;

public class ChestListener implements Listener 
{
    private Main plugin;

    public ChestListener(Main plugin)
    {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void playerOpenChest(InventoryCloseEvent invevent)
    {
        if (invevent.getInventory().getHolder() instanceof Chest || invevent.getInventory().getHolder() instanceof DoubleChest)
        {
            String chestid;
            try
            {
                Chest chest;
                chest = (Chest) invevent.getInventory().getHolder();
                chestid = Utils.getChestIdFromLocation(plugin, chest.getLocation());
            }
            catch (Exception e)
            {
                DoubleChest chest;
                chest = (DoubleChest) invevent.getInventory().getHolder(); 
                chestid = Utils.getChestIdFromLocation(plugin, chest.getLocation());
            }

            if (chestid.equals("")) return;

            Bukkit.broadcastMessage(invevent.getPlayer().getName() + " opened chest #" + chestid);

            /* Update ledger */
            Utils.updateLedger(invevent.getInventory().getContents(), chestid, invevent.getPlayer().getName());

            /* Update chest file */
            Utils.updateChestFile(invevent.getInventory().getContents(), chestid);
        }
    }

}
