package com.socialsite.daffodilvarsity.model;

/**
 * Created by AkshayeJH on 30/11/17.
 */

public class Conversation {

    public boolean seen;
    public long timestamp;
    public boolean onKeyoardOpen;

    public Conversation(){

    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isOnKeyoardOpen() {
        return onKeyoardOpen;
    }

    public void setOnKeyoardOpen(boolean onKeyoardOpen) {
        this.onKeyoardOpen = onKeyoardOpen;
    }

    public Conversation(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }
}
