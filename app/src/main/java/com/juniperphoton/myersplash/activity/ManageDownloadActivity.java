package com.juniperphoton.myersplash.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.DownloadsListAdapter;
import com.juniperphoton.myersplash.callback.DownloadItemsChangedCallback;
import com.juniperphoton.myersplash.model.DownloadItem;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import moe.feng.material.statusbar.StatusBarCompat;


public class ManageDownloadActivity extends AppCompatActivity implements DownloadItemsChangedCallback {

    @Bind(R.id.activity_managedownload_rv)
    RecyclerView mDownloadsRV;

    @Bind(R.id.activity_downloads_no_item_tv)
    TextView mNoItemTV;

    private DownloadsListAdapter mAdapter;
    private RealmChangeListener<DownloadItem> realmListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_managedownload);
        ButterKnife.bind(this);

        realmListener = new RealmChangeListener<DownloadItem>() {
            @Override
            public void onChange(DownloadItem item) {
                mAdapter.updateItem(item);
            }
        };

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Realm.getDefaultInstance().removeAllChangeListeners();
        Realm.getDefaultInstance().close();
    }

    @OnClick(R.id.activity_downloads_more_fab)
    void onClickMore() {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        mAdapter.clear();
        updateNoItemVisibility();
    }

    public void updateNoItemVisibility() {
        if (mAdapter != null) {
            if (mAdapter.getData() != null && mAdapter.getData().size() > 0) {
                mNoItemTV.setVisibility(View.GONE);
                return;
            }
        }
        mNoItemTV.setVisibility(View.VISIBLE);
    }

    private void initViews() {
        final ArrayList<DownloadItem> arrayList = new ArrayList<>();

        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<DownloadItem> items = realm.where(DownloadItem.class).findAll();
                if (items.size() > 0) {
                    for (DownloadItem item : items) {
                        item.addChangeListener(realmListener);
                        arrayList.add(item);
                    }
                }
            }
        });

        if (mAdapter == null) {
            mAdapter = new DownloadsListAdapter(arrayList, this);
            mAdapter.setCallback(this);
        }

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mDownloadsRV.setLayoutManager(layoutManager);
        mDownloadsRV.getItemAnimator().setChangeDuration(0);
        mDownloadsRV.setAdapter(mAdapter);

        updateNoItemVisibility();
    }

    @Override
    public void onDataChanged() {
        updateNoItemVisibility();
    }
}
