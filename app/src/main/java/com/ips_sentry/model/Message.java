package com.ips_sentry.model;

/**
 * Created by comsol on 20-Jun-16.
 * {
 * "id": 241,
 * "type": "in",
 * "toUserId": 102,
 * "fromUserId": 1,
 * "subject": "",
 * "body": "This is another test",
 * "sent": "\/Date(1468504258780)\/",
 * "read": "\/Date(1468520608575)\/"
 * }
 */
public class Message {
    int id;
    String type;
    int toUserId;
    int fromUserId;
    String subject;
    String sent;
    String read;
    String body;
    boolean isReplied = false;
    boolean isSeen;

    public boolean isReplied() {
        return isReplied;
    }

    public void setReplied(boolean replied) {
        isReplied = replied;
    }

    public Message(String body, boolean isSeen) {
        this.body = body;
        this.isSeen = isSeen;
    }

    public Message(String body, boolean isSeen,String type) {
        this.body = body;
        this.isSeen = isSeen;
        this.type = type;
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


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }
}
