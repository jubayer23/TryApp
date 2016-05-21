package com.ips_sentry.model;

/**
 * Created by comsol on 9/10/2015.
 */
public class Venue {

    private String venueName;
    private String logo;
    private String venueAddress;
    private int siteId;
    Double latitude;
    Double longitude;

    public Venue(Double longitude, String venueName, String logo, String venueAddress, int siteId, Double latitude) {
        this.longitude = longitude;
        this.venueName = venueName;
        this.logo = logo;
        this.venueAddress = venueAddress;
        this.siteId = siteId;
        this.latitude = latitude;
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

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
