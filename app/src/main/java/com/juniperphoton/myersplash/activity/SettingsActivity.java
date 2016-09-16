package com.juniperphoton.myersplash.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.common.Constant;
import com.juniperphoton.myersplash.utils.LocalSettingHelper;
import com.juniperphoton.myersplash.utils.ToastService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moe.feng.material.statusbar.StatusBarCompat;

public class SettingsActivity extends AppCompatActivity {

    private final String TAG = SettingsActivity.class.getName();

    private String[] savingStrings;

    private String[] loadingStrings;

    @Bind(R.id.detail_switch_quick_download)
    Switch mQuickDownloadSwitch;

    @Bind(R.id.saving_quality_tv)
    TextView mSavingQualityTV;

    @Bind(R.id.loading_quality_tv)
    TextView mLoadingTV;

    @Bind(R.id.activity_settings_cacheSize_tv)
    TextView mCacheTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        boolean quickDownload = LocalSettingHelper.getBoolean(this, Constant.QUICK_DOWNLOAD_CONFIG_NAME, false);
        mQuickDownloadSwitch.setChecked(quickDownload);
        mQuickDownloadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LocalSettingHelper.putBoolean(SettingsActivity.this, Constant.QUICK_DOWNLOAD_CONFIG_NAME, isChecked);
            }
        });

        savingStrings = new String[]{getString(R.string.SavingHighest),
                getString(R.string.SavingHigh), getString(R.string.SavingMedium)};

        loadingStrings = new String[]{getString(R.string.LoadingLarge),
                getString(R.string.LoadingSmall), getString(R.string.LoadingThumbnail)};

        final int savingChoice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1);
        mSavingQualityTV.setText(savingStrings[savingChoice]);

        final int loadingChoice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0);
        mLoadingTV.setText(loadingStrings[loadingChoice]);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.settings_cleanup_cv)
    public void clearUp() {
        Fresco.getImagePipeline().clearCaches();
        ToastService.sendShortToast("All clear :D");
        mCacheTV.setText("0 MB");
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.settings_saving_quality_cv)
    void setSavingQuality() {
        final int choice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1);

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(getString(R.string.SavingQuality));
        builder.setSingleChoiceItems(savingStrings, choice,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalSettingHelper.putInt(SettingsActivity.this, Constant.SAVING_QUALITY_CONFIG_NAME, which);
                        dialog.dismiss();
                        mSavingQualityTV.setText(savingStrings[which]);
                    }
                });
        builder.show();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.settings_loading_quality_cv)
    void setLoadingQuality() {
        final int choice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(getString(R.string.LoadingQuality));
        builder.setSingleChoiceItems(loadingStrings, choice,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalSettingHelper.putInt(SettingsActivity.this, Constant.LOADING_QUALITY_CONFIG_NAME, which);
                        dialog.dismiss();
                        mLoadingTV.setText(loadingStrings[which]);
                    }
                });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCacheTV.setText(String.valueOf(ImagePipelineFactory.getInstance().getMainFileCache().getSize() / 1024 / 1024) + " MB");
    }
}
