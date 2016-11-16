package com.latmod.cursegraph;

import java.util.Comparator;

/**
 * Created by LatvianModder on 16.11.2016.
 */
public class Points
{
    public static final Comparator<Points> SORT_BY_TIME = (o1, o2) -> Long.compare(o1.time, o2.time);
    public static final Comparator<Points> SORT_BY_POINTS = (o1, o2) -> Double.compare(o2.points, o1.points);

    private final long time;
    private final double points;

    public Points(long t, double p)
    {
        time = t;
        points = p;
    }

    public long getTime()
    {
        return time;
    }

    public double getPoints()
    {
        return points;
    }

    public boolean equalsPoints(Points p)
    {
        return time == p.time && points == p.points;
    }

    public int hashCode()
    {
        return Long.hashCode(time) ^ Double.hashCode(points);
    }

    public boolean equals(Object o)
    {
        return o == this || (o instanceof Points && ((Points) o).equalsPoints(this));
    }

    public String toString()
    {
        return time + ":" + points;
    }
}