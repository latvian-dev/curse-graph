package com.latmod.cursegraph;

import com.google.gson.JsonElement;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public interface IJsonObject
{
    void fromJson(JsonElement e);

    JsonElement toJson();
}