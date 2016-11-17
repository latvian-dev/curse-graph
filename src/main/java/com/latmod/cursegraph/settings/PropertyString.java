package com.latmod.cursegraph.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public class PropertyString implements IProperty
{
    private String value;

    public PropertyString(String def)
    {
        value = def;
    }

    @Override
    public void fromJson(JsonElement e)
    {
        value = e.getAsString();
    }

    @Override
    public JsonElement toJson()
    {
        return new JsonPrimitive(value);
    }

    @Override
    public String getString()
    {
        return value;
    }

    @Override
    public int getInt()
    {
        return Integer.parseInt(value);
    }

    @Override
    public double getDouble()
    {
        return Double.parseDouble(value);
    }
}