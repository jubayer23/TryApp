package com.ips_sentry.model;

/**
 * Created by comsol on 03-Mar-16.
 */
public class SentryIndividual {

    //"Lat":33.817918,"Lng":-118.298932,"Label":"bkindividual3"

    Double Lat;
    Double Lng;
    String Label;

    public SentryIndividual(Double lat, Double lng, String label) {
        Lat = lat;
        Lng = lng;
        Label = label;
    }

    public Double getLat() {
        return Lat;
    }

    public void setLat(Double lat) {
        Lat = lat;
    }

    public Double getLng() {
        return Lng;
    }

    public void setLng(Double lng) {
        Lng = lng;
    }

    public String getLabel() {
        return Label;
    }

    public void setLabel(String label) {
        Label = label;
    }


    @Override
    public String toString() {
        return "SentryIndividual{" +
                "Lat=" + Lat +
                ", Lng=" + Lng +
                ", Label='" + Label + '\'' +
                '}';
    }
}
