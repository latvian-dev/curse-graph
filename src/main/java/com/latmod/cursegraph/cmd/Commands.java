package com.latmod.cursegraph.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public class Commands
{
    private static final Map<String, ICommand> COMMAND_MAP = new HashMap<>();
    private static final Map<String, ICommand> ALIAS_COMMAND_MAP = new HashMap<>();
    private static List<String> COMMAND_LIST;

    public static void add(String id, ICommand cmd, String... al)
    {
        COMMAND_MAP.put(id, cmd);

        for(String s : al)
        {
            ALIAS_COMMAND_MAP.put(s, cmd);
        }
    }

    public static void init()
    {
        add("help", new CmdHelp(), "?");
        add("stop", new CmdStop(), "shutdown", "exit");

        List<String> list = new ArrayList<>(COMMAND_MAP.keySet());
        Collections.sort(list);
        COMMAND_LIST = Collections.unmodifiableList(list);
    }

    public static List<String> getCommands()
    {
        return COMMAND_LIST;
    }

    public static ICommand getCommand(String cmd)
    {
        ICommand command = COMMAND_MAP.get(cmd);

        if(command == null)
        {
            command = ALIAS_COMMAND_MAP.get(cmd);
        }

        return command;
    }

    public static String runCommand(String line) throws Exception
    {
        String[] cmd = line.split(" ");
        ICommand command = getCommand(cmd[0]);

        if(command == null)
        {
            return "Unknown command '" + cmd[0] + "'!";
        }

        String[] args = new String[cmd.length - 1];

        if(args.length > 0)
        {
            System.arraycopy(cmd, 1, args, 0, args.length);
        }

        return command.onCommand(args);
    }
}