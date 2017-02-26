package com.juniperphoton.myersplash.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CompoundButton;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.common.Constant;
import com.juniperphoton.myersplash.utils.LocalSettingHelper;
import com.juniperphoton.myersplash.utils.ToastService;
import com.juniperphoton.myersplash.widget.SettingsItemLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class SettingsActivity extends BaseActivity {
    private final static String TAG = "SettingsActivity";

    private String[] mSavingStrings;

    private String[] mLoadingStrings;

    @BindView(R.id.setting_save_quality)
    SettingsItemLayout mSavingQualityItem;

    @BindView(R.id.setting_load_quality)
    SettingsItemLayout mLoadingQualityItem;

    @BindView(R.id.setting_clear_cache)
    SettingsItemLayout mClearCacheItem;

    @BindView(R.id.setting_quick_download)
    SettingsItemLayout mQuickDownloadItem;

    @BindView(R.id.setting_scroll_appbar)
    SettingsItemLayout mScrollBarItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        boolean quickDownload = LocalSettingHelper.getBoolean(this, Constant.QUICK_DOWNLOAD_CONFIG_NAME, false);
        mQuickDownloadItem.setChecked(quickDownload);
        mQuickDownloadItem.setOnCheckedListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LocalSettingHelper.putBoolean(SettingsActivity.this, Constant.QUICK_DOWNLOAD_CONFIG_NAME, isChecked);
            }
        });

        boolean scrollBar = LocalSettingHelper.getBoolean(this, Constant.SCROLL_TOOLBAR, true);
        mScrollBarItem.setChecked(scrollBar);
        mScrollBarItem.setOnCheckedListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                LocalSettingHelper.putBoolean(SettingsActivity.this, Constant.SCROLL_TOOLBAR, isChecked);
            }
        });

        mSavingStrings = new String[]{getString(R.string.SavingHighest),
                getString(R.string.SavingHigh), getString(R.string.SavingMedium)};

        mLoadingStrings = new String[]{getString(R.string.LoadingLarge),
                getString(R.string.LoadingSmall), getString(R.string.LoadingThumbnail)};

        final int savingChoice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1);
        mSavingQualityItem.setContent(mSavingStrings[savingChoice]);

        final int loadingChoice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0);
        mLoadingQualityItem.setContent(mLoadingStrings[loadingChoice]);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.setting_quick_download)
    public void toggleQuickDownload(View view) {
        mQuickDownloadItem.setChecked(!mQuickDownloadItem.getChecked());
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.setting_scroll_appbar)
    public void toggleToolbarScrolling(View view) {
        mScrollBarItem.setChecked(!mScrollBarItem.getChecked());
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.setting_clear_cache)
    public void clearUp(View view) {
        Fresco.getImagePipeline().clearCaches();
        ToastService.sendShortToast("All clear :D");
        mClearCacheItem.setContent("0 MB");
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.setting_clear_database)
    public void clearDatabase(View view) {
        ToastService.sendShortToast("All clear :D");
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.setting_save_quality)
    void setSavingQuality(View view) {
        final int choice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1);

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(getString(R.string.SavingQuality));
        builder.setSingleChoiceItems(mSavingStrings, choice,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalSettingHelper.putInt(SettingsActivity.this, Constant.SAVING_QUALITY_CONFIG_NAME, which);
                        dialog.dismiss();
                        mSavingQualityItem.setContent(mSavingStrings[which]);
                    }
                });
        builder.show();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.setting_load_quality)
    void setLoadingQuality(View view) {
        final int choice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(getString(R.string.LoadingQuality));
        builder.setSingleChoiceItems(mLoadingStrings, choice,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalSettingHelper.putInt(SettingsActivity.this, Constant.LOADING_QUALITY_CONFIG_NAME, which);
                        dialog.dismiss();
                        mLoadingQualityItem.setContent(mLoadingStrings[which]);
                    }
                });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mClearCacheItem.setContent(String.valueOf(ImagePipelineFactory.getInstance().getMainFileCache().getSize() / 1024 / 1024) + " MB");
    }
}
