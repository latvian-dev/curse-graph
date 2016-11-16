package com.latmod.cursegraph;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LatvianModder on 16.11.2016.
 */
public class CurseGraph
{
    private static File folder;

    public static void main(String[] args) throws Exception
    {
        MainArgs.init(args);
        String folderPath = MainArgs.getArg(MainArgs.FOLDER);
        folder = new File(folderPath.isEmpty() ? "cursegraph" : folderPath);

        String localCursePointsFile = MainArgs.getArg(MainArgs.CURSE_POINTS_FILE);

        if(!localCursePointsFile.isEmpty())
        {
            Map<String, Project> projects = new HashMap<>();
            double totalPoints = 0D;

            System.out.println("Loading transaction data from local file...");
            for(Element e : Jsoup.parse(new File(localCursePointsFile), "UTF-8").select(".transactions"))
            {
                long time = Long.parseLong(e.select("abbr").get(0).attributes().get("data-epoch"));

                for(Element e1 : e.select("li"))
                {
                    Element e2 = e1.getElementsByTag("a").get(0);
                    String id = e2.html();
                    Project p = projects.get(id);

                    if(p == null)
                    {
                        p = new Project(id, e2.attributes().get("href"));
                        projects.put(id, p);
                    }

                    double points = Double.parseDouble(e1.getElementsByTag("b").get(0).html());
                    p.addPoints(new Points(time, points));
                    totalPoints += points;
                }
            }

            List<Project> list = new ArrayList<>(projects.values());
            Collections.sort(list, Project.SORT_BY_POINTS);

            for(Project p : list)
            {
                System.out.println(p.getName() + ": $ " + Utils.round(p.getTotalPoints() * 0.05D) + " [" + Utils.round(p.getTotalPoints()) + " points] :: " + p.getUrl());
            }

            System.out.println("Total: $ " + Utils.round(totalPoints * 0.05D));
        }

        Settings.load();

        // start the Thread here //
    }

    public static File getFolder()
    {
        return folder;
    }
}