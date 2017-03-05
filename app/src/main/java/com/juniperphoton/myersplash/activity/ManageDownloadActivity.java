package com.juniperphoton.myersplash.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.DownloadsListAdapter;
import com.juniperphoton.myersplash.model.DownloadItem;
import com.juniperphoton.myersplash.utils.DeviceUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.juniperphoton.myersplash.utils.DisplayUtil.getDimenInPixel;

public class ManageDownloadActivity extends BaseActivity
        implements DownloadsListAdapter.DownloadStateChangedCallback {
    @BindView(R.id.activity_manage_download_rv)
    RecyclerView mDownloadsRV;

    @BindView(R.id.activity_downloads_no_item_tv)
    TextView mNoItemTV;

    @BindView(R.id.activity_downloads_more_fab)
    FloatingActionButton mMoreFAB;

    private DownloadsListAdapter mAdapter;
    private RealmChangeListener<DownloadItem> mItemStatusListener;
    private RealmChangeListener<RealmResults<DownloadItem>> mListener = new RealmChangeListener<RealmResults<DownloadItem>>() {
        @Override
        public void onChange(RealmResults<DownloadItem> element) {
            updateNoItemVisibility();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managedownload);
        ButterKnife.bind(this);

        mItemStatusListener = new RealmChangeListener<DownloadItem>() {
            @Override
            public void onChange(DownloadItem item) {
                Log.d("manage", "onChange");
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
                RealmResults<DownloadItem> items = realm.where(DownloadItem.class)
                        .equalTo("mStatus", status).findAll();
                for (DownloadItem item : items) {
                    item.removeChangeListener(mItemStatusListener);
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
        List<DownloadItem> downloadItems = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        RealmResults<DownloadItem> items = realm.where(DownloadItem.class).findAll();
        for (DownloadItem item : items) {
            downloadItems.add(item);
            item.addChangeListener(mItemStatusListener);
        }
        items.addChangeListener(mListener);

        realm.commitTransaction();

        if (mAdapter == null) {
            mAdapter = new DownloadsListAdapter(downloadItems, this);
            mAdapter.setCallback(this);
        } else {
            mAdapter.refreshItems(downloadItems);
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
        ((SimpleItemAnimator) mDownloadsRV.getItemAnimator()).setSupportsChangeAnimations(false);
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
