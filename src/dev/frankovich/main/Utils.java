package dev.frankovich.main;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Utils 
{
    private static final String DATAFILEPATH = "./plugins/ChestLogger/data.json";
    private static final String CHESTFILEPATH = "./plugins/ChestLogger/Chests/";

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
    private static JSONObject getOldJSON()
    {
        String str;
        try
        {
            Path path = Path.of(DATAFILEPATH); 
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

    /* Creates a new chest entry to be watched by a player
     * @param playername - the name of the player watching the chest
     * @param uuid - the UUID of the player watching the chest 
     * @param stack - all of the items in the chest 
     * @param x, y, z - the coordinates of the chest
    */
    public static int newChestEntry(Main plugin, String playername, String uuid, ItemStack[] stack, int x, int y, int z)
    {
        /* Data File */
        JSONObject oldobj = getOldJSON(); 
        JSONObject jsonobj = new JSONObject(); 
        JSONObject details = new JSONObject(); 
        details.put("Player", playername);
        details.put("UUID", uuid);
        details.put("X", Integer.toString(x));
        details.put("Y", Integer.toString(y));
        details.put("Z", Integer.toString(z));
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
            FileWriter writer = new FileWriter(CHESTFILEPATH + nextID + ".json");
            writer.write(items.toJSONString());
            writer.close();
        }
        catch (IOException e)
        {
            Bukkit.getLogger().info("[ChestLogger] Could not write to file \"" + CHESTFILEPATH + nextID + ".json\"");
        }

        return 0;
    }

}
