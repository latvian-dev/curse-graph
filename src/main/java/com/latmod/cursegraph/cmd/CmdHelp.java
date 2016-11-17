package com.latmod.cursegraph.cmd;

import com.latmod.cursegraph.CurseGraph;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public class CmdHelp implements ICommand
{
    @Override
    public String commandInfo()
    {
        return "[command]";
    }

    @Override
    public String onCommand(String[] args) throws Exception
    {
        if(args.length == 0)
        {
            CurseGraph.log(String.join(" | ", Commands.getCommands()));
        }
        else
        {
            ICommand command = Commands.getCommand(args[0]);

            if(command == null)
            {
                return "Unknown command '" + args[0] + "'!";
            }
            else if(command.commandInfo().isEmpty())
            {
                return "No info found";
            }

            CurseGraph.log(args[0] + ": " + command.commandInfo());
        }

        return "";
    }
}