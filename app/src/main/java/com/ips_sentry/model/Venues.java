package com.ips_sentry.model;

import java.util.List;

/**
 * Created by comsol on 9/10/2015.
 */
public class Venues {

    String status;
    List<Venue> venues;

    public Venues(String status, List<Venue> venues) {
        this.status = status;
        this.venues = venues;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Venue> getVenues() {
        return venues;
    }

    public void setVenues(List<Venue> venues) {
        this.venues = venues;
    }
}
