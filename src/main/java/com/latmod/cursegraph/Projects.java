package com.latmod.cursegraph;

import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TLongLongHashMap;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LatvianModder on 17.11.2016.
 */
public class Projects
{
    public static final Map<String, String> PROJECTS = new HashMap<>();
    public static final TLongDoubleHashMap CURSE_POINTS = new TLongDoubleHashMap();
    public static final TLongLongHashMap DOWNLOADS = new TLongLongHashMap();

    static // Temp
    {
        PROJECTS.put("ftb-utilities", "ftb-utilities");
        PROJECTS.put("ftblib", "ftblib");
        PROJECTS.put("xpteleporters", "xpteleporters");
        PROJECTS.put("mercurius", "mercurius");
        PROJECTS.put("errormod", "errormod");
    }

    public static boolean updateCursePoints() throws Exception
    {
        String localCursePointsFile = CurseGraph.getArg(CurseGraph.CURSE_POINTS_FILE);

        if(!localCursePointsFile.isEmpty())
        {
            Map<String, Project> projects = new HashMap<>();
            double totalPoints = 0D;

            CurseGraph.log("Loading transaction data from local file...");
            for(Element e : Jsoup.parse(new File(localCursePointsFile), "UTF-8").select(".transactions"))
            {
                long time = Long.parseLong(e.select("abbr").first().attributes().get("data-epoch"));

                for(Element e1 : e.select("li"))
                {
                    Element e2 = e1.getElementsByTag("a").first();
                    String id = e2.html();
                    Project p = projects.get(id);

                    if(p == null)
                    {
                        p = new Project(id, e2.attributes().get("href"));
                        projects.put(id, p);
                    }

                    double points = Double.parseDouble(e1.getElementsByTag("b").first().html());
                    p.addPoints(new Points(time, points));
                    totalPoints += points;
                }
            }

            List<Project> list = new ArrayList<>(projects.values());
            Collections.sort(list, Project.SORT_BY_POINTS);

            for(Project p : list)
            {
                CurseGraph.log(p.getName() + ": $ " + Utils.round(p.getTotalPoints() * 0.05D) + " [" + Utils.round(p.getTotalPoints()) + " points] :: " + p.getUrl());
            }

            CurseGraph.log("Total: $ " + Utils.round(totalPoints * 0.05D));
            return true;
        }

        return false;
    }

    public static void updateDownloads() throws Exception
    {
        CurseGraph.log("Updating all projects...");

        for(String projectID : PROJECTS.values())
        {
            try
            {
                CurseGraph.log("Updating '" + projectID + "' project data...");
                Connection connection = Jsoup.connect("https://minecraft.curseforge.com/projects/" + projectID);
                connection.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
                CurseGraph.log("Downloads: " + totalDownloads(connection.get()));
            }
            catch(Exception ex1)
            {
                CurseGraph.err("Failed to update '" + projectID + "' data: " + ex1);
            }
        }

        CurseGraph.log("Done");
    }

    private static String totalDownloads(Document doc)
    {
        for(Element label : doc.select(".info-label"))
        {
            if(label.html().equals("Total Downloads"))
            {
                return label.parent().select(".info-data").first().html().replace(",", "");
            }
        }

        return "";
    }
}
