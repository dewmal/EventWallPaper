package com.juniperphoton.myersplash.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.ThanksToAdapter;
import com.juniperphoton.myersplash.utils.DeviceUtil;
import com.juniperphoton.myersplash.utils.PackageUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.version_tv)
    TextView mVersionTextView;

    @BindView(R.id.thanks_to_list)
    RecyclerView mList;

    @BindView(R.id.blank_footer)
    View mBlank;

    private ThanksToAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        updateVersion();
        initThanks();

        if (!DeviceUtil.hasNavigationBar(this)) {
            mBlank.setVisibility(View.GONE);
        }
    }

    private void initThanks() {
        mAdapter = new ThanksToAdapter(this);
        List<String> list = new ArrayList<>();
        String[] strs = getResources().getStringArray(R.array.thanks_array);
        for (String str : strs) {
            list.add(str);
        }
        mAdapter.refresh(list);
        mList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mList.setAdapter(mAdapter);
    }

    private void updateVersion() {
        mVersionTextView.setText(PackageUtil.getVersionName(this));
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.email_rl)
    void onClickEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dengweichao@hotmail.com"}); // recipients

        String SHARE_SUBJECT = "MyerSplash for Android %s feedback";
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(SHARE_SUBJECT, PackageUtil.getVersionName(this)));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(emailIntent);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.activity_about_rate_rl)
    void onClickRate() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.activity_about_donate_rl)
    void onClickDonate() {
        if (AlipayZeroSdk.hasInstalledAlipayClient(this)) {
            AlipayZeroSdk.startAlipayClient(this, "aex09127b4dbo4o7fbvcyb0");
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.github_layout)
    void onClickGitHub() {
        Uri uri = Uri.parse("https://github.com/JuniperPhoton/MyerSplashAndroid");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
