package dev.frankovich.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.minecraft.network.syncher.DataWatcher.Item;
import net.minecraft.world.level.material.Material;

public class Utils 
{
    private static final String DATAFILEPATH = "./plugins/ChestLogger/data.json";
    private static final String CHESTFILEPATH = "./plugins/ChestLogger/Chests/";
    private static final String LEDGERFILEPATH = "./plugins/ChestLogger/Ledgers/";
    private static Main plugin; 

    public Utils(Main plugin)
    {
        Utils.plugin = plugin;
    }

    /* Returns a 6 character string containing a chest ID that hasn't been used before */ 
    public static String getNextID()
    {
        int id = plugin.getConfig().getInt("currentid");
        plugin.getConfig().set("currentid", id + 1);
        plugin.saveConfig();
        return String.format("%06d", id); 
    }

    /* Returns json object from json file designated by the path given 
     * @param pathstr - path to get json from
    */
    private static JSONObject getJSON(String pathstr)
    {
        String str;
        try
        {
            Path path = Path.of(pathstr); 
            str = Files.readString(path); 
        }
        catch (IOException e)
        {
            Bukkit.getLogger().info("[ChestLogger] No old JSON data found!");
            return null; 
        }

        try
        {
            JSONParser p = new JSONParser();
            JSONObject jo = (JSONObject) p.parse(str); 
            return jo;
        }
        catch (Exception e)
        {
            Bukkit.getLogger().info("[ChestLogger] Something wrong with parser");
            return null;
        }

    }

    /* Combines the two jsons together. Why isn't there a method for this already? idk. 
     * @param newjson - the json object appended 
     * @param oldjson - the json object new json is appended to  
    */
    private static String combineJSON(JSONObject newjson, JSONObject oldjson) 
    {
        if (oldjson == null) { return newjson.toJSONString(); } 

        String oldstr = oldjson.toJSONString();
        String newstr = newjson.toJSONString();
        oldstr = oldstr.substring(0, oldstr.length()-1);
        newstr = newstr.substring(1);
        return oldstr.concat("," + newstr);
    }

    /* Updates chest file from the item stack given
     * @param stack - item stack that contains chest contents
     * @param filepath - the path to the file that is being updated
    */
    public static void updateChestFile(ItemStack[] stack, String id)
    {
        JSONObject items = new JSONObject();
        JSONArray jsonitems = new JSONArray();
        JSONArray jsonamounts = new JSONArray();
        for (ItemStack s : stack)
		{
			if (s == null) continue;
            jsonitems.add(s.getType().name().toLowerCase());
            jsonamounts.add(Integer.toString(s.getAmount()));
		}
        items.put("item", jsonitems);
        items.put("amount", jsonamounts);

        try
        {
            FileWriter writer = new FileWriter(CHESTFILEPATH + id + ".json");
            writer.write(items.toJSONString());
            writer.close();
        }
        catch (IOException e)
        {
            Bukkit.getLogger().info("[ChestLogger] Could not write to file \"" + CHESTFILEPATH + id + ".json" + "\"");
        }
    }

    /* Update the ledger for the chest for the list command
     * @param stack - the NEW/EDITED inventory of the chest
     * @param id - the id of the chest
     * @param playername - the player that mutated the chest
    */
    public static void updateLedger(ItemStack[] stack, String id, String playername)
    {
        JSONObject obj = getJSON(CHESTFILEPATH + id + ".json");

        /* Old Items */
        JSONArray olditemsjson = (JSONArray) obj.get("item");
        JSONArray olditemamountsjson = (JSONArray) obj.get("amount");
        ArrayList<String> olditemnames = new ArrayList<String>();
        ArrayList<String> olditemamounts = new ArrayList<String>();
        for (int i = 0; i < olditemsjson.size(); ++i)
        {
            int index = olditemnames.indexOf((String) olditemsjson.get(i));
            if (index == -1)
            {
                olditemnames.add((String) olditemsjson.get(i));
                olditemamounts.add((String) olditemamountsjson.get(i));
            }
            else
            {
                int total = Integer.parseInt(olditemamounts.get(index)) + Integer.parseInt((String) olditemamountsjson.get(i));
                olditemamounts.set(index, Integer.toString(total));
            }
        }

        /* Edited/New Items */
        ArrayList<String> editeditems = new ArrayList<String>();
        ArrayList<String> editeditemamounts = new ArrayList<String>();
        for (ItemStack s : stack) 
        { 
            if (s == null) continue; 
            int index = editeditems.indexOf(s.getType().name().toLowerCase());
            if (index == -1)
            {
            editeditems.add(s.getType().name().toLowerCase()); 
            editeditemamounts.add(Integer.toString(s.getAmount())); 
            }
            else
            {
                int total = Integer.parseInt(editeditemamounts.get(index)) + s.getAmount();
                editeditemamounts.set(index, Integer.toString(total));
            }
        }

        String date = java.time.LocalDate.now().toString();
        FileWriter fw;
        BufferedWriter bw;

        try
        {
            fw = new FileWriter(LEDGERFILEPATH + id + ".txt", true);
            bw = new BufferedWriter(fw);
            bw.write(date + "," + playername + ",OPEN");
            bw.newLine();
        }
        catch (IOException e)
        {
            Bukkit.getLogger().info("[ChestLogger] IOException when writing to ledger #" + id);
            return;
        }

        /* Compare Amounts */
        for (String itemname : olditemnames)
        {
            int oldindex = olditemnames.indexOf(itemname); 
            int newindex = editeditems.indexOf(itemname);
            if (newindex == -1)
            {
                try
                {
                    bw.write(date + "," + playername + ",TOOK," + itemname + "," + olditemamounts.get(oldindex)); 
                    bw.newLine();
                }
                catch(IOException e)
                {
                    Bukkit.getLogger().info("[ChestLogger] IOException when writing to ledger #" + id);
                    return;
                }
                continue;
            }

            int oldamt = Integer.parseInt(olditemamounts.get(oldindex));
            int newamt = Integer.parseInt(editeditemamounts.get(newindex));

            if (oldamt != newamt)
            {
                int difference = newamt - oldamt;
                try
                {
                    bw.write(date + "," + playername + ",EDIT," + itemname + "," + Integer.toString(difference)); 
                    bw.newLine();
                }
                catch (IOException e)
                {
                    Bukkit.getLogger().info("[ChestLogger] IOException when writing to ledger #" + id);
                    return;
                }
            }
        }

        for (String itemname : editeditems)
        {
            int oldindex = olditemnames.indexOf(itemname); 
            int newindex = editeditems.indexOf(itemname);
            if (oldindex == -1)
            {
                try
                {
                    bw.write(date + "," + playername + ",ADD," + itemname + "," + editeditemamounts.get(newindex)); 
                    bw.newLine();
                }
                catch(IOException e)
                {
                    Bukkit.getLogger().info("[ChestLogger] IOException when writing to ledger #" + id);
                    return;
                }
                continue;
            }
               
        }

        try
        {
            bw.close();
            fw.close();
        }
        catch (IOException e)
        {
            Bukkit.getLogger().info("[ChestLogger] IOException when closing ledger #" + id);
        }

    }

    /* Creates a new chest entry to be watched by a player
     * @param playername - the name of the player watching the chest
     * @param uuid - the UUID of the player watching the chest 
     * @param stack - all of the items in the chest 
     * @param x, y, z - the coordinates of the chest
     * @param dc - is it a double chest (xz) (+=) means that the other chest is at x+1
    */
    public static int newChestEntry(String playername, String uuid, ItemStack[] stack, int x, int y, int z, String dc)
    {
        /* Data File */
        JSONObject oldobj = getJSON(DATAFILEPATH); 
        JSONObject jsonobj = new JSONObject(); 
        JSONObject details = new JSONObject(); 
        details.put("Player", playername);
        details.put("UUID", uuid);
        details.put("X", Integer.toString(x));
        details.put("Y", Integer.toString(y));
        details.put("Z", Integer.toString(z));
        details.put("DoubleLocation", dc);
        String nextID = getNextID();
        jsonobj.put(nextID, details);

        String combined = combineJSON(jsonobj, oldobj); 

        try
        {
            FileWriter writer = new FileWriter(DATAFILEPATH);
            writer.write(combined);
            writer.close();
        }
        catch (IOException e)
        {
            Bukkit.getLogger().info("[ChestLogger] Could not write to file \"" + DATAFILEPATH + "\"");
        }

        /* Chest File */
        updateChestFile(stack, nextID);
        return 0;
    }

    /* Returns chest id if location is in the data.json file
     * @param location - the location to check
    */
    public static String getChestIdFromLocation(Location location)
    {
        int counter = 1;
        int max = plugin.getConfig().getInt("currentid");
        JSONObject obj = getJSON(DATAFILEPATH);

        for (; counter < max; counter++)
        {
            String idstr = String.format("%06d", counter);
            JSONObject data = (JSONObject) obj.get(idstr);
            if (data == null) { continue; }

            int addx = 0;
            int addz = 0;
            if (((String) data.get("DoubleLocation")).charAt(0) == '+') { addx = 1; }
            else if (((String) data.get("DoubleLocation")).charAt(0) == '-') { addx = -1; }
            else if (((String) data.get("DoubleLocation")).charAt(1) == '-') { addz = -1; }
            else if (((String) data.get("DoubleLocation")).charAt(1) == '+') { addz = 1; }

            /* Check if x, y, z match */
            boolean found = false;

            if (Integer.valueOf((String) data.get("X")) == location.getBlockX()
                && Integer.valueOf((String) data.get("Z")) == location.getBlockZ()
                && Integer.valueOf((String) data.get("Y")) == location.getBlockY()) 
            {
                found = true;
            }

            if (Integer.valueOf((String) data.get("X")) + addx == location.getBlockX()
                && Integer.valueOf((String) data.get("Y")) == location.getBlockY()
                && Integer.valueOf((String) data.get("Z")) + addz == location.getBlockZ()) 
            {
                found = true;
            }

            if (!found) continue;

            return idstr;
        }

        return "";
    }

    /* Sends player messages of the chests they are watching
     * @param p - player requesting messages
    */
    public static void sendWatchedChests(Player p)
    {
        JSONObject obj = getJSON(DATAFILEPATH);
        int counter = 1;
        int max = plugin.getConfig().getInt("currentid");
        String uuid = p.getUniqueId().toString();

        for (; counter < max; counter++)
        {
            String idstr = String.format("%06d", counter);
            JSONObject data = (JSONObject) obj.get(idstr);
            if (data == null) { continue; }

            String datauuid = (String) data.get("UUID");

            if (datauuid.equals(uuid))
            {
                String x = (String) data.get("X");
                String y = (String) data.get("Y");
                String z = Integer.toString(Integer.parseInt((String) data.get("Z")) + 1);

                p.sendMessage("§lID: §r" + counter + " | §lLocation: §r(" + x + ", " + y + ", " + z + ")");
            }
        }

        p.sendMessage(" ");

    }

    /* Returns true if chest is already being watched by same player
     * @param p - player trying to watch chest
     * @param b - block object to look for 
    */
    public static boolean chestBeingWatched(Player p, Block b)
    {
        JSONObject obj = getJSON(DATAFILEPATH);
        int counter = 1;
        int max = plugin.getConfig().getInt("currentid");
        String uuid = p.getUniqueId().toString();

        for (; counter < max; counter++)
        {
            String idstr = String.format("%06d", counter);
            JSONObject data = (JSONObject) obj.get(idstr);
            if (data == null) { continue; }
            String datauuid = (String) data.get("UUID");

            if (datauuid.equals(uuid))
            {
                /* yes ik... just trust on this one */
                Integer x = Integer.parseInt((String) data.get("X"));
                Integer y = Integer.parseInt((String) data.get("Y"));
                Integer z = Integer.parseInt((String) data.get("Z"));

                if (x == b.getLocation().getBlockX() 
                    && y == b.getLocation().getBlockY() 
                    && z == b.getLocation().getBlockZ())
                {
                    return true;
                }
                
            }
        }

        return false;
    }

    /* Sends the player the ledger requested 
     * @param p - player requesting ledger 
     * @param idstr - id of the chest
    */
    public static void printLedger(Player p, String idstr) throws IOException
    {
        JSONObject obj = getJSON(DATAFILEPATH);
        JSONObject data = (JSONObject) obj.get(idstr);

        if (data == null)
        {
            p.sendMessage("§c[ChestLogger] Chest #" + idstr + " doesn't exist!");
            return;
        }
        if (!((String) data.get("UUID")).equals(p.getUniqueId().toString()))
        {
            p.sendMessage("§c[ChestLogger] Chest #" + idstr + " is not on your watchlist!");
            return;
        }

        p.sendMessage(" ");
		p.sendMessage("§l[§d§lChest #" + idstr + " Ledger§f§l]"); 
        File ledgerfile = new File(LEDGERFILEPATH + idstr + ".txt");
        FileReader fr = new FileReader(ledgerfile);
        BufferedReader br = new BufferedReader(fr);

        String line;
        while ((line = br.readLine()) != null && !line.equals(""))
        {
            String[] spl = line.split(",");    
            String otherplayerindicator = ""; 
            if (!spl[1].equals(p.getName()))
            {
                otherplayerindicator = "* | "; 
            }

            if (spl[2].equals("OPEN"))
            {
                p.sendMessage("§l" + otherplayerindicator + spl[0] + " §r| " + spl[1] + " opened the chest");
            }
            else if (spl[2].equals("TOOK"))
            {
                p.sendMessage("§c§l" + otherplayerindicator + spl[0] + " §r§c| "+ spl[1] + " took out all " + spl[4] + " " + spl[3]);
            } 
            else if (spl[2].equals("ADD"))
            {
                p.sendMessage("§a§l" + otherplayerindicator + spl[0] + " §r§a| "+ spl[1] + " put in " + spl[4] + " " + spl[3]);
            }
            else if (spl[2].equals("EDIT"))
            {
                if (Integer.parseInt(spl[4]) > 0)
                {
                    p.sendMessage("§a§l" + otherplayerindicator + spl[0] + " §r§a| "+ spl[1] + " put in " + spl[4] + " " + spl[3]);
                }
                else
                {
                    p.sendMessage("§c§l" + otherplayerindicator + spl[0] + " §r§c| "+ spl[1] + " took out " + Integer.toString(Integer.parseInt(spl[4])*-1) + " " + spl[3]);
                }
            }
        }

        fr.close();
        br.close();
        p.sendMessage(" ");
    }

    /* Returns all chest ids that the player can interact with 
     * @param playername - player that is typing the command 
    */
    public static ArrayList<String> getInteractableIds(String playername) 
    {
        JSONObject obj = getJSON(DATAFILEPATH);
        int counter = 1;
        int max = plugin.getConfig().getInt("currentid");
        ArrayList<String> returnme = new ArrayList<String>();

        for (; counter < max; ++counter)
        {
            String idstr = String.format("%06d", counter);
            JSONObject data = (JSONObject) obj.get(idstr);
            if (data == null) { continue; }

            if (((String) data.get("Player")).equals(playername))
            {
                returnme.add(Integer.toString(counter));
            }

        }

        return returnme;
    }

    /* Returns all chest ids that the player can interact with 
     * @param player - player that is requesting clear 
     * @param idstr - the id of the ledger to clear
    */
    public static void clearLedger(Player player, String idstr) throws IOException 
    {
        JSONObject obj = getJSON(DATAFILEPATH);
        JSONObject data = (JSONObject) obj.get(idstr);
        if (data == null)
        {
            player.sendMessage("§c[ChestLogger] Chest #" + idstr + " doesn't exist!");
            return;
        }
        if (!((String) data.get("UUID")).equals(player.getUniqueId().toString()))
        {
            player.sendMessage("§c[ChestLogger] You are not watching this chest"); 
            return;
        }

        FileWriter fw;
        BufferedWriter bw;
        fw = new FileWriter(LEDGERFILEPATH + idstr + ".txt");
        bw = new BufferedWriter(fw);
        bw.write("");
        bw.close();
        fw.close();
    }
    
    /* Deletes all traces of a chest
     * @param player - player that is requesting the delete  
     * @param idstr - the id of the chest to clear
    */
    public static void deleteChest(Player player, String idstr)
    {
        JSONObject obj = getJSON(DATAFILEPATH);
        JSONObject data = (JSONObject) obj.get(idstr);
        if (data == null)
        {
            player.sendMessage("§c[ChestLogger] Chest #" + idstr + " doesn't exist!");
            return;
        }
        if (!((String) data.get("UUID")).equals(player.getUniqueId().toString()))
        {
            player.sendMessage("§c[ChestLogger] You are not watching this chest"); 
            return;
        }

        try
        {
            obj = (JSONObject) new JSONParser().parse(new FileReader(DATAFILEPATH));
            obj.remove(idstr);
            FileWriter writer = new FileWriter(DATAFILEPATH);
            writer.write(obj.toJSONString());
            writer.close();
        }
        catch (Exception e)
        {
            player.sendMessage("§c[ChestLogger] Error removing chest from data. Check with server administrators"); 
            return;
        }

        try
        {
            File delme = new File(LEDGERFILEPATH + idstr + ".txt");
            delme.delete();
            delme = new File(CHESTFILEPATH + idstr + ".json");
            delme.delete();
        }
        catch (Exception e)
        {
            player.sendMessage("§c[ChestLogger] Error removing chest from data. Check with server administrators"); 
            return;
        }

		player.sendMessage("[§dChestLogger§f] Removed chest from your watchlist");
    }

    /* Returns "+=" or "=-" depending where double is
     * @param w - world the chest could be in
     * @param ch - other chest 
    */
    public static String getDoubleLocation(World w, Chest ch)
    {
        int x = ch.getLocation().getBlockX();
        int y = ch.getLocation().getBlockY();
        int z = ch.getLocation().getBlockZ();

        Block b = w.getBlockAt(x+1, y, z);
        if (b.getType().equals(org.bukkit.Material.CHEST))
        {
            if (hasSameInventory(ch.getInventory(), ((Chest) b.getState()).getInventory()))
            {
                return "+=";
            }
        }

        b = w.getBlockAt(x-1, y, z); 
        if (b.getType().equals(org.bukkit.Material.CHEST))
        {
            if (hasSameInventory(ch.getInventory(), ((Chest) b.getState()).getInventory()))
            {
                return "-=";
            }
        }

        b = w.getBlockAt(x, y, z+1); 
        if (b.getType().equals(org.bukkit.Material.CHEST))
        {
            if (hasSameInventory(ch.getInventory(), ((Chest) b.getState()).getInventory()))
            {
                return "=+";
            }
        }

        b = w.getBlockAt(x, y, z-1); 
        if (b.getType().equals(org.bukkit.Material.CHEST))
        {
            if (hasSameInventory(ch.getInventory(), ((Chest) b.getState()).getInventory()))
            {
                return "=-";
            }
        }

        return "==";
    }

    /* Returns if inventory have same contents
     * @param one - first inventory
     * @param two - second inventory
    */
    public static boolean hasSameInventory(Inventory one, Inventory two)
    {
        ItemStack[] ones = one.getContents();
        ItemStack[] twos = two.getContents();
        for (int i = 0; i < ones.length; ++i)
        {
            if (ones[i] == null) continue;
            String f = ones[i].getType().name();
            String s = twos[i].getType().name();
            if (!f.equals(s))
            {
                return false;
            }
        }

        return true;
    }

}
