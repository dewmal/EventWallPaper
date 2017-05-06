package com.juniperphoton.myersplash.activity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.CompoundButton
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.common.Constant
import com.juniperphoton.myersplash.event.RefreshAllEvent
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import com.juniperphoton.myersplash.utils.ToastService
import com.juniperphoton.myersplash.widget.SettingsItemLayout
import io.realm.Realm
import org.greenrobot.eventbus.EventBus

@Suppress("UNUSED", "UNUSED_PARAMETER")
class SettingsActivity : BaseActivity() {
    private var savingStrings: Array<String>? = null

    private var loadingStrings: Array<String>? = null

    @BindView(R.id.setting_save_quality)
    @JvmField var savingQualityItem: SettingsItemLayout? = null

    @BindView(R.id.setting_load_quality)
    @JvmField var loadingQualityItem: SettingsItemLayout? = null

    @BindView(R.id.setting_clear_cache)
    @JvmField var clearCacheItem: SettingsItemLayout? = null

    @BindView(R.id.setting_quick_download)
    @JvmField var quickDownloadItem: SettingsItemLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)

        val quickDownload = LocalSettingHelper.getBoolean(this, Constant.QUICK_DOWNLOAD_CONFIG_NAME, false)
        quickDownloadItem!!.checked = quickDownload
        quickDownloadItem!!.setOnCheckedListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            LocalSettingHelper.putBoolean(this@SettingsActivity, Constant.QUICK_DOWNLOAD_CONFIG_NAME, isChecked)
        })

        savingStrings = arrayOf(getString(R.string.SavingHighest), getString(R.string.SavingHigh),
                getString(R.string.SavingMedium))

        loadingStrings = arrayOf(getString(R.string.LoadingLarge), getString(R.string.LoadingSmall),
                getString(R.string.LoadingThumbnail))

        val savingChoice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1)
        savingQualityItem!!.setContent(savingStrings!![savingChoice])

        val loadingChoice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0)
        loadingQualityItem!!.setContent(loadingStrings!![loadingChoice])
    }

    @OnClick(R.id.setting_quick_download)
    fun toggleQuickDownload(view: View) {
        quickDownloadItem!!.checked = !quickDownloadItem!!.checked
        EventBus.getDefault().post(RefreshAllEvent())
    }

    @OnClick(R.id.setting_clear_cache)
    fun clearUp(view: View) {
        Fresco.getImagePipeline().clearCaches()
        ToastService.sendShortToast("All clear :D")
        clearCacheItem!!.setContent("0 MB")
        EventBus.getDefault().post(RefreshAllEvent())
    }

    @OnClick(R.id.setting_clear_database)
    fun clearDatabase(view: View) {
        ToastService.sendShortToast("All clear :D")
        RealmCache.getInstance().executeTransaction(Realm::deleteAll)
    }

    @OnClick(R.id.setting_save_quality)
    internal fun setSavingQuality(view: View) {
        val choice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1)

        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setTitle(getString(R.string.SavingQuality))
        builder.setSingleChoiceItems(savingStrings, choice
        ) { dialog, which ->
            LocalSettingHelper.putInt(this@SettingsActivity, Constant.SAVING_QUALITY_CONFIG_NAME, which)
            dialog.dismiss()
            savingQualityItem!!.setContent(savingStrings!![which])
        }
        builder.show()
    }

    @OnClick(R.id.setting_load_quality)
    internal fun setLoadingQuality(view: View) {
        val choice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0)

        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setTitle(getString(R.string.LoadingQuality))
        builder.setSingleChoiceItems(loadingStrings, choice
        ) { dialog, which ->
            LocalSettingHelper.putInt(this@SettingsActivity, Constant.LOADING_QUALITY_CONFIG_NAME, which)
            dialog.dismiss()
            loadingQualityItem!!.setContent(loadingStrings!![which])
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        clearCacheItem!!.setContent("${ImagePipelineFactory.getInstance().mainFileCache.size / 1024 / 1024} MB")
    }

    companion object {
        private val TAG = "SettingsActivity"
    }
}
