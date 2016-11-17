package com.latmod.cursegraph.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public class PropertyBool implements IProperty
{
    private boolean value;

    public PropertyBool(boolean def)
    {
        value = def;
    }

    @Override
    public void fromJson(JsonElement e)
    {
        value = e.getAsBoolean();
    }

    @Override
    public JsonElement toJson()
    {
        return new JsonPrimitive(value);
    }

    @Override
    public String getString()
    {
        return value ? "true" : "false";
    }

    @Override
    public boolean getBool()
    {
        return value;
    }
}