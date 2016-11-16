package com.latmod.cursegraph;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;

/**
 * Created by LatvianModder on 16.11.2016.
 */
public class Utils
{
    public static double round(double d)
    {
        return Math.round(d * 100D) / 100D;
    }

    public static JsonElement fromJsonFile(File file)
    {
        //FIXME
        return new JsonObject();
    }

    public static void toJsonFile(File file, JsonElement element)
    {
        //FIXME
    }
}