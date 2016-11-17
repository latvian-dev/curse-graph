package com.latmod.cursegraph.settings;

import com.latmod.cursegraph.IJsonObject;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public interface IProperty extends IJsonObject
{
    default String getString()
    {
        return "";
    }

    default boolean getBool()
    {
        return !getString().isEmpty();
    }

    default int getInt()
    {
        return getBool() ? 1 : 0;
    }

    default double getDouble()
    {
        return getInt();
    }
}