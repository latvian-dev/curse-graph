package com.latmod.cursegraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by LatvianModder on 16.11.2016.
 */
public class Project
{
    public static final Comparator<Project> SORT_BY_NAME = (o1, o2) -> o1.name.compareToIgnoreCase(o2.name);
    public static final Comparator<Project> SORT_BY_POINTS = (o1, o2) -> Double.compare(o2.getTotalPoints(), o1.getTotalPoints());

    private final String name;
    private final String url;
    private final List<Points> points;
    private double totalPoints;

    public Project(String n, String u)
    {
        name = n;
        url = u;
        points = new ArrayList<>();
        totalPoints = 0D;
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public List<Points> getPoints()
    {
        return points;
    }

    public void addPoints(Points p)
    {
        points.add(p);
        totalPoints += p.getPoints();
    }

    public double getTotalPoints()
    {
        return totalPoints;
    }
}