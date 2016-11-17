package com.latmod.cursegraph.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.latmod.cursegraph.CurseGraph;
import com.latmod.cursegraph.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LatvianModder on 16.11.2016.
 */
public class Settings
{
    public static File file;
    private static final Map<String, IProperty> PROPERTY_MAP = new HashMap<>();

    public static final IProperty UPDATE_CHECK = add("update.check", new PropertyBool(true));
    public static final IProperty UPDATE_LATEST = add("update.latest", new PropertyBool(false));
    public static final IProperty GENERAL_UPDATE_INTERVAL = add("general.update_interval", new PropertyDouble(0.25D));
    public static final IProperty LOGGING_ENABLED = add("logging.enabled", new PropertyBool(false));

    public static final IProperty PHP_EXPORT = add("php.export", new PropertyBool(true));

    public static final IProperty PNG_EXPORT = add("png.export", new PropertyBool(true));
    public static final IProperty PNG_WIDTH = add("png.width", new PropertyInt(600));
    public static final IProperty PNG_HEIGHT = add("png.height", new PropertyInt(400));

    public static final IProperty JSON_EXPORT = add("json.export", new PropertyBool(true));
    public static final IProperty JSON_PRETTY = add("json.pretty", new PropertyBool(true));

    public static IProperty add(String id, IProperty property)
    {
        PROPERTY_MAP.put(id, property);
        return property;
    }

    public static void load()
    {
        String path = CurseGraph.getArg(CurseGraph.CONFIG_FILE);
        file = path.isEmpty() ? new File(CurseGraph.INSTANCE.getFolder(), "settings.json") : new File(path);

        JsonElement json = Utils.fromJsonFile(file);
        JsonObject o = Utils.fromJsonTree(json.isJsonObject() ? json.getAsJsonObject() : new JsonObject());

        for(Map.Entry<String, JsonElement> e : o.entrySet())
        {
            IProperty property = PROPERTY_MAP.get(e.getKey());

            if(property != null)
            {
                property.fromJson(e.getValue());
            }
        }

        save();
    }

    public static void save()
    {
        JsonObject o = new JsonObject();
        PROPERTY_MAP.forEach((key, value) -> o.add(key, value.toJson()));
        Utils.toJsonFile(file, Utils.toJsonTree(o.entrySet()), true);
    }
}