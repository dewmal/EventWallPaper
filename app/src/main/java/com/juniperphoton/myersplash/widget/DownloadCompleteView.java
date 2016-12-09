package com.juniperphoton.myersplash.widget;


import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.interfaces.ISetThemeColor;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DownloadCompleteView extends FrameLayout implements ISetThemeColor {

    @Bind(R.id.widget_set_as_rl)
    RelativeLayout SetAsBtn;

    @Bind(R.id.widget_set_as_root_rl)
    RelativeLayout SetAsRL;

    private Uri fileUri;

    public DownloadCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.widget_download_complete_view, this);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.widget_set_as_rl)
    void setAs() {
        if (fileUri != null) {
            File file = new File(fileUri.getPath());
            Uri uri = FileProvider.getUriForFile(App.getInstance(), App.getInstance().getString(R.string.authorities), file);
            Intent intent = WallpaperManager.getInstance(App.getInstance()).getCropAndSetWallpaperIntent(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getInstance().startActivity(intent);
        }
    }

    @Override
    public void setThemeBackColor(int color) {
        SetAsRL.setBackground(new ColorDrawable(color));
    }

    @Override
    public void setThemeForeColor(int color) {

    }
}
