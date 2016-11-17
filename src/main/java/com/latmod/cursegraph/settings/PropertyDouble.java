package com.latmod.cursegraph.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public class PropertyDouble implements IProperty
{
    private double value;

    public PropertyDouble(double def)
    {
        value = def;
    }

    @Override
    public void fromJson(JsonElement e)
    {
        value = e.getAsDouble();
    }

    @Override
    public JsonElement toJson()
    {
        return new JsonPrimitive(value);
    }

    @Override
    public String getString()
    {
        return Double.toString(value);
    }

    @Override
    public boolean getBool()
    {
        return value != 0D;
    }

    @Override
    public int getInt()
    {
        return (int) value;
    }

    @Override
    public double getDouble()
    {
        return value;
    }
}