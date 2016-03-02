package com.ips_sentry.model;

/**
 * Created by comsol on 9/10/2015.
 */
public class Venue {

    private  String venueName;
    private  String logo;
    private String venueAddress;

    public Venue(String venueName, String logo, String venueAddress) {
        this.venueName = venueName;
        this.logo = logo;
        this.venueAddress = venueAddress;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
}
