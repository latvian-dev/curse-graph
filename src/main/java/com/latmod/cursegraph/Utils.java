package com.latmod.cursegraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * Created by LatvianModder on 16.11.2016.
 */
public class Utils
{
    private static final Gson GSON, GSON_PRETTY;

    static
    {
        GsonBuilder builder = new GsonBuilder();
        GSON = builder.create();
        builder.setPrettyPrinting();
        GSON_PRETTY = builder.create();
    }

    public static double round(double d)
    {
        return Math.round(d * 100D) / 100D;
    }

    public static JsonElement fromJsonFile(File file)
    {
        if(!file.exists())
        {
            return JsonNull.INSTANCE;
        }

        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            JsonElement e = new JsonParser().parse(reader);
            reader.close();
            return e;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return JsonNull.INSTANCE;
    }

    public static File create(File file) throws IOException
    {
        if(file.exists())
        {
            return file;
        }
        else if(!file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }

        file.createNewFile();
        return file;
    }

    public static void toJsonFile(File file, JsonElement element, boolean pretty)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(create(file)));
            (pretty ? GSON_PRETTY : GSON).toJson(element, writer);
            writer.flush();
            writer.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static JsonElement fromJsonURL(String s)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(s).openStream()));
            JsonElement e = new JsonParser().parse(reader);
            reader.close();
            return e;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return JsonNull.INSTANCE;
    }

    public static JsonObject fromJsonTree(JsonObject o)
    {
        JsonObject map = new JsonObject();
        fromJsonTree0(map, null, o);
        return map;
    }

    private static void fromJsonTree0(JsonObject map, String id0, JsonObject o)
    {
        for(Map.Entry<String, JsonElement> entry : o.entrySet())
        {
            if(entry.getValue() instanceof JsonObject)
            {
                fromJsonTree0(map, (id0 == null) ? entry.getKey() : (id0 + '.' + entry.getKey()), entry.getValue().getAsJsonObject());
            }
            else
            {
                map.add((id0 == null) ? entry.getKey() : (id0 + '.' + entry.getKey()), entry.getValue());
            }
        }
    }

    public static JsonObject toJsonTree(Collection<Map.Entry<String, JsonElement>> tree)
    {
        JsonObject o1 = new JsonObject();
        tree.forEach(entry -> findGroup(o1, entry.getKey()).add(lastKeyPart(entry.getKey()), entry.getValue()));
        return o1;
    }

    private static String lastKeyPart(String s)
    {
        int idx = s.lastIndexOf('.');
        if(idx != -1)
        {
            return s.substring(idx + 1);
        }

        return s;
    }

    private static JsonObject findGroup(JsonObject parent, String s)
    {
        int idx = s.indexOf('.');
        if(idx != -1)
        {
            String s0 = s.substring(0, idx);
            JsonElement o = parent.get(s0);
            if(o == null)
            {
                o = new JsonObject();
                parent.add(s0, o);
            }

            return findGroup(o.getAsJsonObject(), s.substring(idx + 1, s.length() - 1));
        }

        return parent;
    }
}