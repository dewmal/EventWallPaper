package com.juniperphoton.myersplash.event;

public class RequestSearchEvent {
    public String query;

    public RequestSearchEvent(String q) {
        query = q;
    }
}
