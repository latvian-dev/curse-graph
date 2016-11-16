package com.latmod.cursegraph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by LatvianModder on 16.11.2016.
 */
public class MainArgs
{
    private static final Map<String, String> MAIN_ARGS = new HashMap<>();
    public static final String FOLDER = "folder";
    public static final String CONFIG_FILE = "config-file";
    public static final String CURSE_POINTS_FILE = "curse-points-file";

    public static void init(String[] args)
    {
        Iterator<String> argsItr = Arrays.asList(args).iterator();

        while(argsItr.hasNext())
        {
            String key = argsItr.next();

            if(key.startsWith("--"))
            {
                MAIN_ARGS.put(key.substring(2), argsItr.next());
            }
            else
            {
                MAIN_ARGS.put(key.startsWith("-") ? key.substring(1) : key, "true");
            }
        }
    }

    public static String getArg(String key)
    {
        String v = MAIN_ARGS.get(key);
        return v == null ? "" : v;
    }

    public static boolean hasArg(String key)
    {
        return getArg(key).equals("true");
    }
}