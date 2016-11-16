package com.latmod.cursegraph;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LatvianModder on 16.11.2016.
 */
public class Settings
{
    public static File file;

    public static boolean checkForUpdates;
    public static double updateInterval;

    public static class PHP
    {
        public static boolean export;
    }

    public static class PNG
    {
        public static boolean export;
        public static int width;
        public static int height;
    }

    public static class JSON
    {
        public static boolean export;
        public static boolean pretty;
    }

    public static class CursePoints
    {
        public String username;
        public String password;
        public boolean exportJson;
        public boolean exportPng;
    }

    public static List<CursePoints> cursePoints;

    public static void load()
    {
        String path = MainArgs.getArg(MainArgs.CONFIG_FILE);
        file = path.isEmpty() ? new File(CurseGraph.getFolder(), "settings.json") : new File(path);

        JsonObject o = Utils.fromJsonFile(file).getAsJsonObject();

        checkForUpdates = o.has("check_for_updates") && o.get("check_for_updates").getAsBoolean();
        updateInterval = o.has("update_interval") ? o.get("update_interval").getAsDouble() : 1D;

        if(o.has("php"))
        {
            JsonObject o1 = o.get("php").getAsJsonObject();
            PHP.export = o1.has("export") && o1.get("export").getAsBoolean();
        }

        if(o.has("png"))
        {
            JsonObject o1 = o.get("png").getAsJsonObject();
            PNG.export = o1.has("export") && o1.get("export").getAsBoolean();
            PNG.width = o1.has("width") ? o1.get("width").getAsInt() : 600;
            PNG.height = o1.has("height") ? o1.get("height").getAsInt() : 400;
        }

        if(o.has("json"))
        {
            JsonObject o1 = o.get("json").getAsJsonObject();
            JSON.export = o1.has("export") && o1.get("export").getAsBoolean();
            JSON.pretty = o1.has("pretty") && o1.get("pretty").getAsBoolean();
        }

        if(o.has("curse_points"))
        {
            JsonArray a = o.get("curse_points").getAsJsonArray();
            cursePoints = new ArrayList<>(a.size());

            for(JsonElement e : a)
            {
                JsonObject o1 = e.getAsJsonObject();
                CursePoints cp = new CursePoints();
                cursePoints.add(cp);
            }
        }
        else
        {
            cursePoints = new ArrayList<>();
        }

        save();
    }

    public static void save()
    {
        JsonObject o = new JsonObject();
        o.add("check_for_updates", new JsonPrimitive(checkForUpdates));
        o.add("update_interval", new JsonPrimitive(updateInterval));

        JsonObject o1 = new JsonObject();
        o1.add("export", new JsonPrimitive(PHP.export));
        o.add("php", o1);

        o1 = new JsonObject();
        o1.add("export", new JsonPrimitive(PNG.export));
        o1.add("width", new JsonPrimitive(PNG.width));
        o1.add("height", new JsonPrimitive(PNG.height));
        o.add("png", o1);

        o1 = new JsonObject();
        o1.add("export", new JsonPrimitive(JSON.export));
        o.add("json", o1);

        JsonArray a = new JsonArray();

        for(CursePoints cp : cursePoints)
        {
            o1 = new JsonObject();
            o1.add("username", new JsonPrimitive(cp.username));
            o1.add("password", new JsonPrimitive(cp.password));
            o1.add("export_json", new JsonPrimitive(cp.exportJson));
            o1.add("export_png", new JsonPrimitive(cp.exportPng));
            a.add(o1);
        }

        o.add("curse_points", a);

        Utils.toJsonFile(file, o);
    }
}