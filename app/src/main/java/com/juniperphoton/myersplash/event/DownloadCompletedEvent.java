package com.juniperphoton.myersplash.event;

import android.net.Uri;


public class DownloadCompletedEvent {
    private Uri mUri;

    public DownloadCompletedEvent(Uri uri) {
        mUri = uri;
    }

    public Uri getUri() {
        return mUri;
    }
}
