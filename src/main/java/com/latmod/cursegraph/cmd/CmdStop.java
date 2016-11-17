package com.latmod.cursegraph.cmd;

import com.latmod.cursegraph.CurseGraph;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public class CmdStop implements ICommand
{
    @Override
    public String onCommand(String[] args) throws Exception
    {
        CurseGraph.log("Stopping CurseGraph...");
        CurseGraph.INSTANCE.stop();
        return "";
    }
}