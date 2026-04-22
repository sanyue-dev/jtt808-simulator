package cn.org.hentai.simulator.domain.model;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by houcheng when 2019/1/6.
 */
public class DrivePlan implements Serializable
{
    LinkedList<Point> routePoints;

    public LinkedList<Point> getRoutePoints()
    {
        return routePoints;
    }

    public DrivePlan setRoutePoints(LinkedList<Point> routePoints)
    {
        this.routePoints = routePoints;
        return this;
    }
}
