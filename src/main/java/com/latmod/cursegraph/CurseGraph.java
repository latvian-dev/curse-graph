package com.latmod.cursegraph;

import com.google.gson.JsonObject;
import com.latmod.cursegraph.cmd.Commands;
import com.latmod.cursegraph.settings.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by LatvianModder on 16.11.2016.
 */
public enum CurseGraph implements Runnable
{
    INSTANCE;
    private static final Map<String, String> MAIN_ARGS = new HashMap<>();
    private static final StringBuilder LOG_BUILDER = new StringBuilder();
    private static final List<String> LOG = new ArrayList<>();

    public static final String FOLDER = "folder";
    public static final String CONFIG_FILE = "config-file";
    public static final String PROJECTS_FILE = "projects-file";
    public static final String CURSE_POINTS_FILE = "curse-points-file";

    private String version = "0.0.0";
    private File folder;
    private boolean running = true;
    public long time;
    private BufferedReader console;

    private Thread consoleThread = new Thread("ConsoleIn")
    {
        public void run()
        {
            Commands.init();
            console = new BufferedReader(new InputStreamReader(System.in));

            while(running)
            {
                try
                {
                    String line = console.readLine().trim();

                    if(!line.isEmpty())
                    {
                        String s = Commands.runCommand(line);

                        if(!s.isEmpty())
                        {
                            err(s);
                        }
                    }
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    };

    public static void main(String[] args) throws Exception
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

        new Thread(CurseGraph.INSTANCE, "CurseGraph").start();
    }

    private static void log0(Object info, String post)
    {
        LOG_BUILDER.append('[');
        Calendar c = Calendar.getInstance();
        int n = c.get(Calendar.HOUR_OF_DAY);
        if(n < 10)
        {
            LOG_BUILDER.append(0);
        }
        LOG_BUILDER.append(n);
        LOG_BUILDER.append(':');
        n = c.get(Calendar.MINUTE);
        if(n < 10)
        {
            LOG_BUILDER.append(0);
        }
        LOG_BUILDER.append(n);
        LOG_BUILDER.append(':');
        n = c.get(Calendar.SECOND);
        if(n < 10)
        {
            LOG_BUILDER.append(0);
        }
        LOG_BUILDER.append(n);
        LOG_BUILDER.append(post);
        LOG_BUILDER.append(info);
        System.out.println(LOG_BUILDER);

        if(Settings.LOGGING_ENABLED.getBool())
        {
            LOG.add(LOG_BUILDER.toString());
        }

        LOG_BUILDER.setLength(0);
    }

    public static void log(Object info)
    {
        log0(info, "][LOG]: ");
    }

    public static void err(Object info)
    {
        log0(info, "][ERR]: ");
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

    public File getFolder()
    {
        return folder;
    }

    @Override
    public void run()
    {
        String folderPath = getArg(FOLDER);
        folder = new File(folderPath.isEmpty() ? "cursegraph" : folderPath);

        Settings.load();

        if(Settings.UPDATE_CHECK.getBool())
        {
            try
            {
                JsonObject o = Utils.fromJsonURL("http://cursegraph.latmod.com/versions").getAsJsonObject();
                String v = Settings.UPDATE_LATEST.getBool() ? o.get("latest").getAsString() : o.get("recommended").getAsString();

                if(!v.equals(version))
                {
                    log("Update available! Current: " + version + (Settings.UPDATE_LATEST.getBool() ? ", Latest: " : ", Recommended: ") + v);
                }
            }
            catch(Exception ex)
            {
                err("Failed to check update: " + ex);
            }
        }

        try
        {
            if(Projects.updateCursePoints())
            {
                return;
            }

            consoleThread.setDaemon(true);
            consoleThread.start();

            long lastUpdateTime = 0L;

            while(running)
            {
                time = System.currentTimeMillis();

                if(lastUpdateTime < time)
                {
                    lastUpdateTime = time + (long) (Settings.GENERAL_UPDATE_INTERVAL.getDouble() * 3600000L);
                    Projects.updateDownloads();
                }
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void stop()
    {
        running = true;
        consoleThread = null;

        try
        {
            console.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }

        console = null;
    }
}