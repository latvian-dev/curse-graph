package com.latmod.cursegraph.cmd;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public interface ICommand
{
    default String commandInfo()
    {
        return "";
    }

    String onCommand(String[] args) throws Exception;
}