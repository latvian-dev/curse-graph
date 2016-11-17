package com.latmod.cursegraph.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public class PropertyInt implements IProperty
{
    private int value;

    public PropertyInt(int def)
    {
        value = def;
    }

    @Override
    public void fromJson(JsonElement e)
    {
        value = e.getAsInt();
    }

    @Override
    public JsonElement toJson()
    {
        return new JsonPrimitive(value);
    }

    @Override
    public String getString()
    {
        return Integer.toString(value);
    }

    @Override
    public boolean getBool()
    {
        return value != 0;
    }

    @Override
    public int getInt()
    {
        return value;
    }

    @Override
    public double getDouble()
    {
        return value;
    }
}