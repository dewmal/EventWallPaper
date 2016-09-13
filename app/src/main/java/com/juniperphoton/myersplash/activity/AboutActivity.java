package com.juniperphoton.myersplash.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.juniperphoton.myersplash.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moe.feng.material.statusbar.StatusBarCompat;

public class AboutActivity extends AppCompatActivity {

    private final String SHARE_SUBJECT = "MyerSplash for Android %s feedback";

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
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(SHARE_SUBJECT, getResources().getString(R.string.Version)));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(emailIntent);
    }
}
