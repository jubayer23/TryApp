package com.ips_sentry.model;

/**
 * Created by comsol on 20-Jun-16.
 */
public class Message {
    String body;
    boolean isSeen;


    public Message(String body, boolean isSeen) {
        this.body = body;
        this.isSeen = isSeen;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
