package dev.frankovich.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Utils 
{
    private static final String DATAFILEPATH = "./plugins/ChestLogger/data.json";
    private static final String CHESTFILEPATH = "./plugins/ChestLogger/Chests/";
    private static final String LEDGERFILEPATH = "./plugins/ChestLogger/Ledgers/";

    /* Returns a 6 character string containing a chest ID that hasn't been used before 
     * @param plugin - plugin to get the config from 
    */ 
    public static String getNextID(Main plugin)
    {
        int id = plugin.getConfig().getInt("currentid");
        plugin.getConfig().set("currentid", id + 1);
        return String.format("%06d", id); 
    }

    /* Returns the previous data in the file */
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
        JSONArray olditems = (JSONArray) obj.get("item");
        JSONArray olditemamounts = (JSONArray) obj.get("amount");

        ArrayList<String> editeditems = new ArrayList<String>();
        ArrayList<String> editeditemamounts = new ArrayList<String>();
        for (ItemStack s : stack) 
        { 
            if (s == null) continue; 
            editeditems.add(s.getType().name().toLowerCase()); 
            editeditemamounts.add(Integer.toString(s.getAmount())); 
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

        for (int i = 0; i < olditems.size(); ++i)
        {
            String itemname = (String) olditems.get(i);
            int itemamount = Integer.valueOf((String) olditemamounts.get(i)); // yes ik it looks stupid just trust //
            int index = editeditems.indexOf(itemname);

            if (index == -1)
            {
                /* Item taken out of chest */
                try
                {
                    bw.write(date + "," + playername + ",TOOKOUT," + itemname);
                    bw.newLine();
                }
                catch (IOException e)
                {
                    Bukkit.getLogger().info("[ChestLogger] IOException when writing to ledger #" + id);
                }
                continue;
            }

            if (itemamount != Integer.valueOf(editeditemamounts.get(index)))
            {
                /* Item amount mutated */
                int difference = Integer.valueOf(editeditemamounts.get(index)) - itemamount;
                try
                {
                    if (difference < 0)
                    {
                        bw.write(date + "," + playername + ",TOOK," + itemname + "," + Integer.toString(difference));
                        bw.newLine();
                    }
                    else
                    {
                        bw.write(date + "," + playername + ",ADDED," + itemname + "," + Integer.toString(difference));
                        bw.newLine();
                    }
                }
                catch (IOException e)
                {
                    Bukkit.getLogger().info("[ChestLogger] IOException when writing to ledger #" + id);
                }
            }
        }

        /* Now check for any new new items inside the chest */
        for (int i = 0; i < editeditems.size(); ++i)
        {
            String item = (String) editeditems.get(i);
            boolean found = true;
            for (int k = 0; k < olditems.size(); ++k)
            {
                if (item.equals((String) olditems.get(k)))
                {
                    found = false;
                    break;
                }
            }

            if (found)
            {
                try
                {
                    bw.write(date + "," + playername + ",ADD," + item + "," + (String) editeditemamounts.get(i));
                    bw.newLine();
                }
                catch (IOException e)
                {
                    Bukkit.getLogger().info("[ChestLogger] IOException when writing to ledger #" + id);
                }
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
     * @param plugin - the plugin
     * @param playername - the name of the player watching the chest
     * @param uuid - the UUID of the player watching the chest 
     * @param stack - all of the items in the chest 
     * @param x, y, z - the coordinates of the chest
     * @param dc - is it a double chest
    */
    public static int newChestEntry(Main plugin, String playername, String uuid, ItemStack[] stack, int x, int y, int z, boolean dc)
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
        details.put("IsDouble", Boolean.toString(dc));
        String nextID = getNextID(plugin);
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
    public static String getChestIdFromLocation(Main plugin, Location location)
    {
        int counter = 1;
        int max = plugin.getConfig().getInt("currentid");
        JSONObject obj = getJSON(DATAFILEPATH);

        for (; counter < max; counter++)
        {
            String idstr = String.format("%06d", counter);
            JSONObject data = (JSONObject) obj.get(idstr);

            /* Check if x, y, z match */
            if (Integer.valueOf((String) data.get("X")) != location.getBlockX()) continue;
            if (Integer.valueOf((String) data.get("Z")) != location.getBlockZ()) continue;
            if (Integer.valueOf((String) data.get("Y")) != location.getBlockY()) continue;

            return idstr;
        }

        return "";
    }

}
