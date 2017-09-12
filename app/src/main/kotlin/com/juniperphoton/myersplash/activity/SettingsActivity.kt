package com.juniperphoton.myersplash.activity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.event.RefreshUIEvent
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import com.juniperphoton.myersplash.utils.ToastService
import com.juniperphoton.myersplash.widget.SettingsItemLayout
import io.realm.Realm
import org.greenrobot.eventbus.EventBus

@Suppress("unused", "unused_parameter")
class SettingsActivity : BaseActivity() {
    companion object {
        private const val TAG = "SettingsActivity"

        private val savingQualitySettingsKey = App.instance.getString(R.string.preference_key_saving_quality)
        private val listQualitySettingsKey = App.instance.getString(R.string.preference_key_list_quality)
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

    @BindView(R.id.recommendation_settings)
    lateinit var recommendationSettings: SettingsItemLayout

    @BindView(R.id.recommendation_preview)
    lateinit var recommendationPreview: View

    @BindView(R.id.clear_cache_settings)
    lateinit var clearCacheItem: SettingsItemLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)

        quickDownloadSettings.onCheckedChanged = {
            EventBus.getDefault().post(RefreshUIEvent())
        }

        recommendationSettings.onCheckedChanged = {
            recommendationPreview.visibility = if (it) View.VISIBLE else View.GONE
        }

        if (!recommendationSettings.checked) {
            recommendationPreview.visibility = View.GONE
        }

        savingStrings = arrayOf(getString(R.string.settings_saving_highest), getString(R.string.settings_saving_high),
                getString(R.string.settings_saving_medium))

        loadingStrings = arrayOf(getString(R.string.settings_loading_large), getString(R.string.settings_loading_small),
                getString(R.string.settings_loading_thumb))

        val savingChoice = LocalSettingHelper.getInt(this, savingQualitySettingsKey, 1)
        savingQualitySettings.content = savingStrings[savingChoice]

        val loadingChoice = LocalSettingHelper.getInt(this, listQualitySettingsKey, 0)
        loadingQualitySettings.content = loadingStrings[loadingChoice]
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
        val choice = LocalSettingHelper.getInt(this, savingQualitySettingsKey, 1)

        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setTitle(getString(R.string.settings_saving_quality))
        builder.setSingleChoiceItems(savingStrings, choice) { dialog, which ->
            LocalSettingHelper.putInt(this@SettingsActivity, savingQualitySettingsKey, which)
            dialog.dismiss()
            savingQualitySettings.content = savingStrings[which]
        }
        builder.show()
    }

    @OnClick(R.id.loading_quality_settings)
    internal fun setLoadingQuality(view: View) {
        val choice = LocalSettingHelper.getInt(this, listQualitySettingsKey, 0)

        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setTitle(getString(R.string.settings_loading_quality))
        builder.setSingleChoiceItems(loadingStrings, choice) { dialog, which ->
            LocalSettingHelper.putInt(this@SettingsActivity, listQualitySettingsKey, which)
            dialog.dismiss()
            loadingQualitySettings.content = loadingStrings[which]
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        clearCacheItem.content = "${ImagePipelineFactory.getInstance().mainFileCache.size / 1024 / 1024} MB"
    }
}
