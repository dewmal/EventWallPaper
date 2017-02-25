package com.juniperphoton.myersplash.event;

public class ScrollToTopEvent {
    public int categoryId;
    public boolean requestRefresh;

    public ScrollToTopEvent(int id, boolean refresh) {
        categoryId = id;
        requestRefresh = refresh;
    }
}
