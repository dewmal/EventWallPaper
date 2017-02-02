package com.juniperphoton.myersplash.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.DownloadsListAdapter;
import com.juniperphoton.myersplash.model.DownloadItem;
import com.juniperphoton.myersplash.utils.DeviceUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import moe.feng.material.statusbar.StatusBarCompat;

import static com.juniperphoton.myersplash.utils.DisplayUtil.getDimenInPixel;

public class ManageDownloadActivity extends AppCompatActivity
        implements DownloadsListAdapter.DownloadStateChangedCallback {
    @BindView(R.id.activity_manage_download_rv)
    RecyclerView mDownloadsRV;

    @BindView(R.id.activity_downloads_no_item_tv)
    TextView mNoItemTV;

    @BindView(R.id.activity_downloads_more_fab)
    FloatingActionButton mMoreFAB;

    private DownloadsListAdapter mAdapter;
    private RealmChangeListener<DownloadItem> mRealmListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_managedownload);
        ButterKnife.bind(this);

        mRealmListener = new RealmChangeListener<DownloadItem>() {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.clear_options_title).
                setItems(R.array.delete_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0: {
                                deleteFromRealm(DownloadItem.DOWNLOAD_STATUS_DOWNLOADING);
                            }
                            break;
                            case 1: {
                                deleteFromRealm(DownloadItem.DOWNLOAD_STATUS_OK);
                            }
                            break;
                            case 2: {
                                deleteFromRealm(DownloadItem.DOWNLOAD_STATUS_FAILED);
                            }
                            break;
                        }
                    }
                })
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    private void deleteFromRealm(final int status) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<DownloadItem> items = realm.where(DownloadItem.class).equalTo("mStatus", status).findAll();
                for (DownloadItem item : items) {
                    item.removeChangeListener(mRealmListener);
                    item.deleteFromRealm();
                }
            }
        });
        initViews();
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
        final ArrayList<DownloadItem> list = new ArrayList<>();

        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<DownloadItem> items = realm.where(DownloadItem.class).findAll();
                if (items.size() > 0) {
                    for (DownloadItem item : items) {
                        item.addChangeListener(mRealmListener);
                        list.add(0, item);
                    }
                }
            }
        });

        if (mAdapter == null) {
            mAdapter = new DownloadsListAdapter(list, this);
            mAdapter.setCallback(this);
        } else {
            mAdapter.refreshItems(list);
        }

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == mAdapter.getItemCount() - 1) {
                    return 2;
                } else {
                    return 1;
                }
            }
        });
        mDownloadsRV.setLayoutManager(layoutManager);
        mDownloadsRV.getItemAnimator().setChangeDuration(0);
        mDownloadsRV.setAdapter(mAdapter);

        updateNoItemVisibility();

        if (!DeviceUtil.hasNavigationBar(this)) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mMoreFAB.getLayoutParams();
            params.setMargins(0, 0, getDimenInPixel(24, this), getDimenInPixel(24, this));
            mMoreFAB.setLayoutParams(params);
        }
    }

    @Override
    public void onDataChanged() {
        updateNoItemVisibility();
    }

    @Override
    public void onRetryDownload(String id) {
        //DownloadUtil.checkAndDownload(this,m);
    }
}
