package com.ips_sentry.model;

/**
 * Created by comsol on 20-Jan-16.
 */
public class Route {
    String RouteId;
    String RouteName;
    boolean Selected;


    public Route(String routeId, String routeName, boolean selected) {
        RouteId = routeId;
        RouteName = routeName;
        Selected = selected;
    }

    public boolean isSelected() {
        return Selected;
    }

    public void setSelected(boolean selected) {
        Selected = selected;
    }



    public String getRouteId() {
        return RouteId;
    }

    public void setRouteId(String routeId) {
        RouteId = routeId;
    }

    public String getRouteName() {
        return RouteName;
    }

    public void setRouteName(String routeName) {
        RouteName = routeName;
    }

    @Override
    public String toString() {
        return "Route{" +
                "RouteId='" + RouteId + '\'' +
                ", RouteName='" + RouteName + '\'' +
                ", Selected=" + Selected +
                '}';
    }
}
