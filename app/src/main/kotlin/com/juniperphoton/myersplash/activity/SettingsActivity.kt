package com.juniperphoton.myersplash.activity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.common.Constant
import com.juniperphoton.myersplash.event.RefreshUIEvent
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import com.juniperphoton.myersplash.utils.ToastService
import com.juniperphoton.myersplash.widget.SettingsItemLayout
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_settings.*
import org.greenrobot.eventbus.EventBus

@Suppress("unused", "unused_parameter")
class SettingsActivity : BaseActivity() {
    companion object {
        private const val TAG = "SettingsActivity"
    }

    private lateinit var savingStrings: Array<String>
    private lateinit var loadingStrings: Array<String>

    @BindView(R.id.quick_download_settings)
    lateinit var quickDownloadSettings: SettingsItemLayout

    @BindView(R.id.saving_quality_settings)
    lateinit var savingQualitySettings: SettingsItemLayout

    @BindView(R.id.loading_quality_settings)
    lateinit var loadingQualitySettings: SettingsItemLayout

    @BindView(R.id.clear_cache_settings)
    lateinit var clearCacheSettings: SettingsItemLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)

        val quickDownload = LocalSettingHelper.getBoolean(this, Constant.QUICK_DOWNLOAD_CONFIG_NAME, true)
        quickDownloadSettings.checked = quickDownload
        quickDownloadSettings.onCheckedChanged = {
            LocalSettingHelper.putBoolean(this@SettingsActivity, Constant.QUICK_DOWNLOAD_CONFIG_NAME, it)
        }

        savingStrings = arrayOf(getString(R.string.saving_highest), getString(R.string.saving_high),
                getString(R.string.saving_medium))

        loadingStrings = arrayOf(getString(R.string.loading_large), getString(R.string.loading_small),
                getString(R.string.loading_thumb))

        val savingChoice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1)
        savingQualitySettings.content = savingStrings[savingChoice]

        val loadingChoice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0)
        loadingQualitySettings.content = loadingStrings[loadingChoice]
    }

    @OnClick(R.id.quick_download_settings)
    fun toggleQuickDownload(view: View) {
        quickDownloadSettings.checked = !quickDownloadSettings.checked
        EventBus.getDefault().post(RefreshUIEvent())
    }

    @OnClick(R.id.clear_cache_settings)
    fun clearUp(view: View) {
        Fresco.getImagePipeline().clearCaches()
        ToastService.sendShortToast("All clear :D")
        clearCacheSettings.content = "0 MB"
        EventBus.getDefault().post(RefreshUIEvent())
    }

    @OnClick(R.id.setting_clear_database)
    fun clearDatabase(view: View) {
        ToastService.sendShortToast("All clear :D")
        RealmCache.getInstance().executeTransaction(Realm::deleteAll)
    }

    @OnClick(R.id.saving_quality_settings)
    internal fun setSavingQuality(view: View) {
        val choice = LocalSettingHelper.getInt(this, Constant.SAVING_QUALITY_CONFIG_NAME, 1)

        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setTitle(getString(R.string.saving_quality))
        builder.setSingleChoiceItems(savingStrings, choice) { dialog, which ->
            LocalSettingHelper.putInt(this@SettingsActivity, Constant.SAVING_QUALITY_CONFIG_NAME, which)
            dialog.dismiss()
            savingQualitySettings.content = savingStrings[which]
        }
        builder.show()
    }

    @OnClick(R.id.loading_quality_settings)
    internal fun setLoadingQuality(view: View) {
        val choice = LocalSettingHelper.getInt(this, Constant.LOADING_QUALITY_CONFIG_NAME, 0)

        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setTitle(getString(R.string.loading_quality))
        builder.setSingleChoiceItems(loadingStrings, choice
        ) { dialog, which ->
            LocalSettingHelper.putInt(this@SettingsActivity, Constant.LOADING_QUALITY_CONFIG_NAME, which)
            dialog.dismiss()
            loadingQualitySettings.content = loadingStrings[which]
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        clear_cache_settings.content = "${ImagePipelineFactory.getInstance().mainFileCache.size / 1024 / 1024} MB"
    }
}
