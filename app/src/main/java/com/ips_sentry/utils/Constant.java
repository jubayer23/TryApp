package com.ips_sentry.utils;

public class Constant {


    public static final String URL_PREFIX = "http://";

    public static String URL_ENV = URL_PREFIX + "dev.";

    public static String URL_LOGIN = "ips-systems.com/home/MobileAppSignIn";
    public static String URL_GPSUpdate = "IPS-Systems.com/Sentry/MobileAppUpdateLocation";
    public static String URL_TRACKING = "IPS-Systems.com/Sentry/MobileAppNewStatus";

    public static String URL_SIGNOUT = "IPS-Systems.com/home/MobileAppSignOut";

    public static String URL_NEARBYPLACE = "ips-systems.com/Sentry/GetNearbyVenues";


    public static String URL_SHOW_ROUTES = "ips-systems.com/Sentry/GetRoutes";

    public static String URL_SELECT_DESELECT_ROUTES = "ips-systems.com/Sentry/SetRoute";

    public static String URL_BATTERY_DAMAGE = "ips-systems.com/Sentry/LowBatteryAlert";

    public static final String URL_WEBSITE = "http://www.ips-systems.com/";



    public static final String[] gps_interval = {"5", "15", "30", "60", "300", "900", "1800", "3600"};

    public static final String[] url_env = {"dev", "qa", "staging", "prod"};


    public static final String ADMIN_PASSWORD = "1127";

    public static final int ON_VEHICLE = 0;
    public static final int ON_BICYCLE = 1;
    public static final int ON_FOOT = 2;
    public static final int ON_wALKING = 3;
    public static final int ON_STILL = 4;
    public static final int ON_TILTING = 5;
    public static final int ON_RUNNING = 6;
    public static final int ON_UNKNOWN = 7;

    public static String USER_ACTIVITY_IDLE = "Idle";
    public static String USER_ACTIVITY_MOVING = "Moving";
    public static String USER_ACTIVITY_STOPPED = "Stopped";


    public static String getUserActivity(int type) {
        if (type == ON_VEHICLE || type == ON_UNKNOWN || type == ON_RUNNING || type == ON_TILTING || type == ON_wALKING || type == ON_BICYCLE || type == ON_FOOT) {
            return USER_ACTIVITY_MOVING;
        } else {
            return USER_ACTIVITY_IDLE;
        }
    }


}
