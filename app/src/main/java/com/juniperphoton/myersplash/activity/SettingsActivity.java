package com.juniperphoton.myersplash.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.utils.ToastService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
    }

    @OnClick(R.id.activity_settings_clearupbtn)
    public void clearUp() {
        Fresco.getImagePipeline().clearCaches();
        ToastService.sendShortToast("All clear :D");
    }
}
