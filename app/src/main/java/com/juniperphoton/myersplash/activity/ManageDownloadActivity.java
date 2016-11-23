package com.juniperphoton.myersplash.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.DownloadsListAdapter;
import com.juniperphoton.myersplash.event.DownloadEvent;
import com.juniperphoton.myersplash.model.DownloadItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import moe.feng.material.statusbar.StatusBarCompat;


public class ManageDownloadActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.activity_managedownload_rv)
    RecyclerView mDownloadsRV;

    private DownloadsListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_managedownload);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onReceiveMessage(DownloadEvent event) {
        if (mAdapter == null) {
            mAdapter = new DownloadsListAdapter(new ArrayList<DownloadItem>(), this);
        }
    }

    private void initViews() {

        final ArrayList<DownloadItem> arrayList = new ArrayList<>();

        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<DownloadItem> items = realm.where(DownloadItem.class).findAll();
                if (items.size() > 0) {
                    for (DownloadItem item : items) {
                        arrayList.add(item);
                    }
                }
            }
        });

        if (mAdapter == null) {
            mAdapter = new DownloadsListAdapter(arrayList, this);
        }

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);

        mDownloadsRV.setLayoutManager(layoutManager);

        mDownloadsRV.setAdapter(mAdapter);
    }
}
