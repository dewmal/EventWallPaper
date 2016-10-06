package com.juniperphoton.myersplash.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.juniperphoton.myersplash.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import moe.feng.material.statusbar.StatusBarCompat;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.email_rl)
    void emailClick(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dengweichao@hotmail.com"}); // recipients

        String SHARE_SUBJECT = "MyerSplash for Android %s feedback";
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(SHARE_SUBJECT, getResources().getString(R.string.Version)));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(emailIntent);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.activity_about_rate_rl)
    void rateClick(View view) {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
