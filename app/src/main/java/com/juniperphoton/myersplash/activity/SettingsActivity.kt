package com.juniperphoton.myersplash.activity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.CompoundButton

import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.common.Constant
import com.juniperphoton.myersplash.event.RefreshAllEvent
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import com.juniperphoton.myersplash.utils.ToastService
import com.juniperphoton.myersplash.widget.SettingsItemLayout

import org.greenrobot.eventbus.EventBus

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.RealmCache
import io.realm.Realm

class SettingsActivity : BaseActivity() {

    private var mSavingStrings: Array<String>? = null

    private var mLoadingStrings: Array<String>? = null

    @BindView(R.id.setting_save_quality)
    @JvmField var mSavingQualityItem: SettingsItemLayout? = null

    @BindView(R.id.setting_load_quality)
    @JvmField var mLoadingQualityItem: SettingsItemLayout? = null

    @BindView(R.id.setting_clear_cache)
    @JvmField var mClearCacheItem: SettingsItemLayout? = null

    @BindView(R.id.setting_quick_download)
    @JvmField var mQuickDownloadItem: SettingsItemLayout? = null

    @BindView(R.id.setting_scroll_appbar)
    @JvmField var mScrollBarItem: SettingsItemLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)

        val quickDownload = LocalSettingHelper.getBoolean(this, Constant.QUICK_DOWNLOAD_CONFIG_NAME, false)
        mQuickDownloadItem!!.checked = quickDownload
        mQuickDownloadItem!!.setOnCheckedListener(CompoundButton.OnCheckedChangeListener { v, isChecked ->
            LocalSettingHelper.putBoolean(this@SettingsActivity, Constant.QUICK_DOWNLOAD_CONFIG_NAME, isChecked)
        })

        val scrollBar = LocalSettingHelper.getBoolean(this, Constant.SCROLL_TOOLBAR, true)
        mScrollBarItem!!.checked = scrollBar
        mScrollBarItem!!.setOnCheckedListener(CompoundButton.OnCheckedChangeListener { c, isChecked ->
            LocalSettingHelper.putBoolean(this@SettingsActivity, Constant.SCROLL_TOOLBAR, isChecked)
        })

        mSavingStrings = arrayOf(getString(R.string.SavingHighest), getString(R.string.SavingHigh),
                getString(R.string.SavingMedium))

        mLoadingStrings = arrayOf(getString(R.string.LoadingLarge), getString(R.string.LoadingSmall),
                getString(R.string.LoadingThumbnail))

        val savingChoice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1)
        mSavingQualityItem!!.setContent(mSavingStrings!![savingChoice])

        val loadingChoice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0)
        mLoadingQualityItem!!.setContent(mLoadingStrings!![loadingChoice])
    }

    @OnClick(R.id.setting_quick_download)
    fun toggleQuickDownload(view: View) {
        mQuickDownloadItem!!.checked = !mQuickDownloadItem!!.checked
        EventBus.getDefault().post(RefreshAllEvent())
    }

    @OnClick(R.id.setting_scroll_appbar)
    fun toggleToolbarScrolling(view: View) {
        mScrollBarItem!!.checked = !mScrollBarItem!!.checked
    }

    @OnClick(R.id.setting_clear_cache)
    fun clearUp(view: View) {
        Fresco.getImagePipeline().clearCaches()
        ToastService.sendShortToast("All clear :D")
        mClearCacheItem!!.setContent("0 MB")
        EventBus.getDefault().post(RefreshAllEvent())
    }

    @OnClick(R.id.setting_clear_database)
    fun clearDatabase(view: View) {
        ToastService.sendShortToast("All clear :D")
        RealmCache.getInstance().executeTransaction { realm -> realm.deleteAll() }
    }

    @OnClick(R.id.setting_save_quality)
    internal fun setSavingQuality(view: View) {
        val choice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1)

        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setTitle(getString(R.string.SavingQuality))
        builder.setSingleChoiceItems(mSavingStrings, choice
        ) { dialog, which ->
            LocalSettingHelper.putInt(this@SettingsActivity, Constant.SAVING_QUALITY_CONFIG_NAME, which)
            dialog.dismiss()
            mSavingQualityItem!!.setContent(mSavingStrings!![which])
        }
        builder.show()
    }

    @OnClick(R.id.setting_load_quality)
    internal fun setLoadingQuality(view: View) {
        val choice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0)

        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setTitle(getString(R.string.LoadingQuality))
        builder.setSingleChoiceItems(mLoadingStrings, choice
        ) { dialog, which ->
            LocalSettingHelper.putInt(this@SettingsActivity, Constant.LOADING_QUALITY_CONFIG_NAME, which)
            dialog.dismiss()
            mLoadingQualityItem!!.setContent(mLoadingStrings!![which])
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        mClearCacheItem!!.setContent("${ImagePipelineFactory.getInstance().mainFileCache.size / 1024 / 1024} MB")
    }

    companion object {
        private val TAG = "SettingsActivity"
    }
}
