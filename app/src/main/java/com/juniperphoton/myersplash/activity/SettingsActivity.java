package com.juniperphoton.myersplash.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import moe.feng.material.statusbar.StatusBarCompat;

public class SettingsActivity extends AppCompatActivity {

    private final String TAG = SettingsActivity.class.getName();

    private String[] savingStrings;

    private String[] loadingStrings;

    @BindView(R.id.setting_save_quality)
    SettingsItemLayout savingQualityItem;

    @BindView(R.id.setting_load_quality)
    SettingsItemLayout loadingQualityItem;

    @BindView(R.id.setting_clear_cache)
    SettingsItemLayout clearCacheItem;

    @BindView(R.id.setting_quick_download)
    SettingsItemLayout quickDownloadItem;

    @BindView(R.id.setting_scroll_appbar)
    SettingsItemLayout scrollBarItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        boolean quickDownload = LocalSettingHelper.getBoolean(this, Constant.QUICK_DOWNLOAD_CONFIG_NAME, false);
        quickDownloadItem.setChecked(quickDownload);
        quickDownloadItem.setOnCheckedListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LocalSettingHelper.putBoolean(SettingsActivity.this, Constant.QUICK_DOWNLOAD_CONFIG_NAME, isChecked);
            }
        });

        boolean scrollBar = LocalSettingHelper.getBoolean(this, Constant.SCROLL_TOOLBAR, true);
        scrollBarItem.setChecked(scrollBar);
        scrollBarItem.setOnCheckedListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                LocalSettingHelper.putBoolean(SettingsActivity.this, Constant.SCROLL_TOOLBAR, isChecked);
            }
        });

        savingStrings = new String[]{getString(R.string.SavingHighest),
                getString(R.string.SavingHigh), getString(R.string.SavingMedium)};

        loadingStrings = new String[]{getString(R.string.LoadingLarge),
                getString(R.string.LoadingSmall), getString(R.string.LoadingThumbnail)};

        final int savingChoice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1);
        savingQualityItem.setContent(savingStrings[savingChoice]);

        final int loadingChoice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0);
        loadingQualityItem.setContent(loadingStrings[loadingChoice]);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.setting_quick_download)
    public void toggleQuickDownload(View view) {
        quickDownloadItem.setChecked(!quickDownloadItem.getChecked());
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.setting_scroll_appbar)
    public void toggleToolbarScolling(View view) {
        scrollBarItem.setChecked(!scrollBarItem.getChecked());
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.setting_clear_cache)
    public void clearUp(View view) {
        Fresco.getImagePipeline().clearCaches();
        ToastService.sendShortToast("All clear :D");
        clearCacheItem.setContent("0 MB");
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
        builder.setSingleChoiceItems(savingStrings, choice,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalSettingHelper.putInt(SettingsActivity.this, Constant.SAVING_QUALITY_CONFIG_NAME, which);
                        dialog.dismiss();
                        savingQualityItem.setContent(savingStrings[which]);
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
        builder.setSingleChoiceItems(loadingStrings, choice,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocalSettingHelper.putInt(SettingsActivity.this, Constant.LOADING_QUALITY_CONFIG_NAME, which);
                        dialog.dismiss();
                        loadingQualityItem.setContent(loadingStrings[which]);
                    }
                });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        clearCacheItem.setContent(String.valueOf(ImagePipelineFactory.getInstance().getMainFileCache().getSize() / 1024 / 1024) + " MB");
    }
}
