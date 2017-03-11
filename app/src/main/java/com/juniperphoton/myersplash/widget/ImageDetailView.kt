package com.juniperphoton.myersplash.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.WallpaperManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView

import com.facebook.binaryresource.BinaryResource
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.cache.common.CacheKey
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.request.ImageRequest
import com.juniperphoton.flipperviewlib.FlipperView
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.event.DownloadStartedEvent
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.AnimatorListenerImpl
import com.juniperphoton.myersplash.utils.ColorUtil
import com.juniperphoton.myersplash.utils.DownloadItemTransactionUtil
import com.juniperphoton.myersplash.utils.DownloadUtil
import com.juniperphoton.myersplash.utils.ToastService

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.io.File

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.RealmCache
import io.realm.Realm
import io.realm.RealmChangeListener
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.functions.Func1
import rx.schedulers.Schedulers

@Suppress("UNUSED")
class ImageDetailView(private val mContext: Context, attrs: AttributeSet) : FrameLayout(mContext, attrs) {

    private var copyFileForSharing: File? = null

    private var heroStartY = 0
    private var heroEndY = 0

    private var clickedView: View? = null
    private var clickedImage: UnsplashImage? = null

    private var navigationCallback: StateListener? = null

    @BindView(R.id.detail_root_sv)
    @JvmField var detailRootScrollView: ViewGroup? = null

    @BindView(R.id.detail_hero_dv)
    @JvmField var heroDV: SimpleDraweeView? = null

    @BindView(R.id.detail_backgrd_rl)
    @JvmField var detailInfoRootLayout: ViewGroup? = null

    @BindView(R.id.detail_img_rl)
    @JvmField var detailImgRL: ViewGroup? = null

    @BindView(R.id.detail_name_tv)
    @JvmField var nameTextView: TextView? = null

    @BindView(R.id.detail_name_line)
    @JvmField var lineView: View? = null

    @BindView(R.id.detail_photo_by_tv)
    @JvmField var photoByTextView: TextView? = null

    @BindView(R.id.detail_download_fab)
    @JvmField var downloadFAB: FloatingActionButton? = null

    @BindView(R.id.detail_cancel_download_fab)
    @JvmField var cancelDownloadFAB: FloatingActionButton? = null

    @BindView(R.id.detail_share_fab)
    @JvmField var shareFAB: FloatingActionButton? = null

    @BindView(R.id.copy_url_tv)
    @JvmField var copyUrlTextView: TextView? = null

    @BindView(R.id.copied_url_tv)
    @JvmField var copiedUrlTextView: TextView? = null

    @BindView(R.id.copy_url_fl)
    @JvmField var copyLayout: FrameLayout? = null

    @BindView(R.id.copied_url_fl)
    @JvmField var copiedLayout: FrameLayout? = null

    @BindView(R.id.copy_url_flipper_view)
    @JvmField var copyUrlFlipperView: FlipperView? = null

    @BindView(R.id.download_flipper_view)
    @JvmField var downloadFlipperView: FlipperView? = null

    @BindView(R.id.detail_progress_ring)
    @JvmField var progressView: RingProgressView? = null

    @BindView(R.id.detail_set_as_fab)
    @JvmField var setAsFAB: FloatingActionButton? = null

    private var associatedDownloadItem: DownloadItem? = null

    private val realmChangeListener = RealmChangeListener<DownloadItem> { element ->
        when (element.status) {
            DownloadItem.DOWNLOAD_STATUS_DOWNLOADING -> progressView?.setProgress(element.progress)
            DownloadItem.DOWNLOAD_STATUS_FAILED -> downloadFlipperView?.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD)
            DownloadItem.DOWNLOAD_STATUS_OK -> downloadFlipperView?.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD_OK)
        }
    }

    private var animating: Boolean = false
    private var copied: Boolean = false

    init {
        LayoutInflater.from(mContext).inflate(R.layout.detail_content, this, true)
        ButterKnife.bind(this, this)

        initDetailViews()
    }

    fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @OnClick(R.id.detail_name_tv)
    internal fun onClickName() {
        val uri = Uri.parse(clickedImage?.userHomePage)

        val intentBuilder = CustomTabsIntent.Builder()

        intentBuilder.setToolbarColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))

        intentBuilder.setStartAnimations(mContext, R.anim.in_from_right, R.anim.out_from_left)
        intentBuilder.setExitAnimations(mContext, R.anim.in_from_left, R.anim.out_from_right)

        val customTabsIntent = intentBuilder.build()

        customTabsIntent.launchUrl(mContext, uri)
    }

    @OnClick(R.id.copy_url_flipper_view)
    internal fun onClickCopy() {
        if (copied) return
        copied = true

        copyUrlFlipperView?.next()

        val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(mContext.getString(R.string.app_name), clickedImage?.downloadUrl)
        clipboard.primaryClip = clip

        postDelayed({
            copyUrlFlipperView!!.next()
            copied = false
        }, 2000)
    }

    @OnClick(R.id.detail_share_fab)
    internal fun onClickShare() {
        val cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(
                ImageRequest.fromUri(Uri.parse(clickedImage!!.listUrl)), null)

        var localFile: File? = null

        if (cacheKey != null) {
            if (ImagePipelineFactory.getInstance().mainFileCache.hasKey(cacheKey)) {
                val resource = ImagePipelineFactory.getInstance().mainFileCache.getResource(cacheKey)
                localFile = (resource as FileBinaryResource).file
            }
        }

        var copied = false
        if (localFile != null && localFile.exists()) {
            copyFileForSharing = File(DownloadUtil.getGalleryPath(), "Share-" + localFile.name)
            copied = DownloadUtil.copyFile(localFile, copyFileForSharing!!)
        }

        if (copyFileForSharing == null || !copyFileForSharing!!.exists() || !copied) {
            ToastService.sendShortToast(mContext.getString(R.string.something_wrong))
            return
        }

        val shareText = String.format(SHARE_TEXT, clickedImage!!.userName, clickedImage!!.downloadUrl)

        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.action = Intent.ACTION_SEND
        intent.type = "image/jpg"
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(copyFileForSharing))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share")
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        (mContext as Activity).startActivityForResult(Intent.createChooser(intent, "Share"), RESULT_CODE, null)
    }

    private fun initDetailViews() {
        detailRootScrollView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    tryHide()
                }
            }
            true
        }

        detailRootScrollView?.visibility = View.INVISIBLE

        detailInfoRootLayout?.translationY = (-resources.getDimensionPixelOffset(R.dimen.img_detail_info_height)).toFloat()
        downloadFlipperView?.translationX = resources.getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide).toFloat()
        shareFAB?.translationX = resources.getDimensionPixelOffset(R.dimen.share_btn_margin_right_hide).toFloat()

        val valueAnimator = ValueAnimator.ofFloat(0f, 360f)
        valueAnimator.addUpdateListener { animation -> progressView?.rotation = animation.animatedValue as Float }
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 1200
        valueAnimator.repeatMode = ValueAnimator.RESTART
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.start()
    }

    private fun associateWithDownloadItem(item: DownloadItem?) {
        if (item == null) {
            val realm = RealmCache.getInstance()
            realm.beginTransaction()
            associatedDownloadItem = realm.where(DownloadItem::class.java)
                    .equalTo(DownloadItem.ID_KEY, clickedImage!!.id).findFirst()
            realm.commitTransaction()
        }

        associatedDownloadItem?.removeChangeListeners()
        associatedDownloadItem?.addChangeListener(realmChangeListener)
    }

    @OnClick(R.id.detail_download_fab)
    internal fun onClickDownload() {
        Log.d(TAG, "onClickDownload")
        if (clickedImage == null) {
            return
        }
        DownloadUtil.checkAndDownload(mContext as Activity, clickedImage!!)
    }

    @OnClick(R.id.detail_cancel_download_fab)
    internal fun onClickCancelDownload() {
        Log.d(TAG, "onClickCancelDownload")
        if (clickedImage == null) {
            return
        }
        downloadFlipperView!!.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD)

        DownloadItemTransactionUtil.updateStatus(associatedDownloadItem!!, DownloadItem.DOWNLOAD_STATUS_FAILED)
        DownloadUtil.cancelDownload(mContext, clickedImage!!)
    }

    @OnClick(R.id.detail_set_as_fab)
    internal fun onClickSetAsFAB() {
        val url = clickedImage!!.pathForDownload + ".jpg"
        if (url != null) {
            val file = File(url)
            val uri = FileProvider.getUriForFile(App.instance,
                    App.instance.getString(R.string.authorities), file)
            val intent = WallpaperManager.getInstance(App.instance).getCropAndSetWallpaperIntent(uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance.startActivity(intent)
        }
    }

    private fun toggleHeroViewAnimation(startY: Int, endY: Int, show: Boolean) {
        if (show) {
            heroStartY = startY
            heroEndY = endY
        } else {
            downloadFlipperView!!.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD)
        }

        val valueAnimator = ValueAnimator()
        valueAnimator.setIntValues(startY, endY)
        valueAnimator.duration = 300
        valueAnimator.interpolator = FastOutSlowInInterpolator()
        valueAnimator.addUpdateListener { animation -> detailImgRL!!.translationY = (animation.animatedValue as Int).toFloat() }
        valueAnimator.addListener(object : AnimatorListenerImpl() {
            override fun onAnimationEnd(animation: Animator) {
                if (!show && clickedView != null) {
                    clickedView!!.visibility = View.VISIBLE
                    toggleMaskAnimation(false)
                    clickedView = null
                    clickedImage = null
                    animating = false
                } else {
                    toggleDetailRLAnimation(true)
                    toggleDownloadFlipperViewAnimation(true)
                    toggleShareBtnAnimation(true)
                }
            }
        })
        valueAnimator.start()
    }

    private fun checkDownloadStatus() {
        Observable.just<UnsplashImage>(clickedImage)
                .observeOn(Schedulers.io())
                .map { clickedImage!!.hasDownloaded() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { b ->
                    if (b!!) {
                        downloadFlipperView!!.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD_OK)
                    }
                }
    }

    private val targetY: Int
        get() = ((mContext as Activity).window.decorView.height
                - resources.getDimensionPixelSize(R.dimen.img_detail_height)) / 2

    private fun toggleDetailRLAnimation(show: Boolean) {
        val startY = if (show) -resources.getDimensionPixelOffset(R.dimen.img_detail_info_height) else 0
        val endY = if (show) 0 else -resources.getDimensionPixelOffset(R.dimen.img_detail_info_height)

        val valueAnimator = ValueAnimator()
        valueAnimator.setIntValues(startY, endY)
        valueAnimator.duration = 500
        valueAnimator.interpolator = FastOutSlowInInterpolator()
        valueAnimator.addUpdateListener { animation ->
            detailInfoRootLayout?.translationY = (animation.animatedValue as Int).toFloat()
        }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                animating = true
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!show) {
                    toggleHeroViewAnimation(heroEndY, heroStartY, false)
                } else {
                    animating = false
                }
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        valueAnimator.start()
    }

    private fun toggleDownloadFlipperViewAnimation(show: Boolean) {
        val normalX = resources.getDimensionPixelOffset(R.dimen.download_btn_margin_right)

        val hideX = resources.getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide)

        val valueAnimator = ValueAnimator()
        valueAnimator.setIntValues(if (show) hideX else 0, if (show) 0 else hideX)
        valueAnimator.duration = 700
        valueAnimator.interpolator = FastOutSlowInInterpolator()
        valueAnimator.addUpdateListener { animation -> downloadFlipperView!!.translationX = (animation.animatedValue as Int).toFloat() }
        valueAnimator.start()
    }

    private fun toggleShareBtnAnimation(show: Boolean) {
        val normalX = resources.getDimensionPixelOffset(R.dimen.share_btn_margin_right)

        val hideX = resources.getDimensionPixelOffset(R.dimen.share_btn_margin_right_hide)

        val valueAnimator = ValueAnimator()
        valueAnimator.setIntValues(if (show) hideX else 0, if (show) 0 else hideX)
        valueAnimator.duration = 700
        valueAnimator.interpolator = FastOutSlowInInterpolator()
        valueAnimator.addUpdateListener { animation -> shareFAB!!.translationX = (animation.animatedValue as Int).toFloat() }
        valueAnimator.start()
    }

    private fun toggleMaskAnimation(show: Boolean) {
        val animator = ValueAnimator.ofArgb(if (show) Color.TRANSPARENT else ContextCompat.getColor(mContext, R.color.MaskColor),
                if (show) ContextCompat.getColor(mContext, R.color.MaskColor) else Color.TRANSPARENT)
        animator.duration = 300
        animator.addUpdateListener { animation -> detailRootScrollView!!.background = ColorDrawable(animation.animatedValue as Int) }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                if (show) {
                    navigationCallback?.onShowing()
                } else {
                    navigationCallback?.onHiding()
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (show) {
                    navigationCallback?.onShown()
                } else {
                    detailRootScrollView?.visibility = View.INVISIBLE
                    navigationCallback?.onHidden()
                }
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        animator.start()
    }

    private fun hideDetailPanel() {
        if (animating) return
        toggleDetailRLAnimation(false)
        toggleDownloadFlipperViewAnimation(false)
        toggleShareBtnAnimation(false)
    }

    fun setNavigationCallback(callback: StateListener) {
        navigationCallback = callback
    }

    fun deleteShareFileInDelay() {
        //TODO: Should has a better way to do this.
        Handler().postDelayed({
            if (copyFileForSharing != null && copyFileForSharing!!.exists()) {
                val ok = copyFileForSharing!!.delete()
            }
        }, 30000)
    }

    fun tryHide(): Boolean {
        if (associatedDownloadItem != null && associatedDownloadItem!!.isValid) {
            associatedDownloadItem!!.removeChangeListener(realmChangeListener)
            associatedDownloadItem = null
        }
        if (detailRootScrollView!!.visibility == View.VISIBLE) {
            hideDetailPanel()
            return true
        }
        return false
    }

    /**
     * Show detailed image

     * @param rectF         Original occupied rect
     * *
     * @param unsplashImage Image
     * *
     * @param itemView      View to be clicked
     */
    fun showDetailedImage(rectF: RectF, unsplashImage: UnsplashImage, itemView: View) {
        if (clickedView != null) {
            return
        }
        clickedImage = unsplashImage
        clickedView = itemView
        clickedView!!.visibility = View.INVISIBLE

        detailInfoRootLayout?.background = ColorDrawable(unsplashImage.themeColor)
        val themeColor = unsplashImage.themeColor
        val alpha = Color.alpha(themeColor)

        //Dark
        if (!ColorUtil.isColorLight(themeColor)) {
            copyUrlTextView!!.setTextColor(Color.BLACK)
            val backColor = Color.argb(200, Color.red(Color.WHITE),
                    Color.green(Color.WHITE), Color.blue(Color.WHITE))
            copyLayout!!.setBackgroundColor(backColor)
        } else {
            copyUrlTextView!!.setTextColor(Color.WHITE)
            val backColor = Color.argb(200, Color.red(Color.BLACK),
                    Color.green(Color.BLACK), Color.blue(Color.BLACK))
            copyLayout!!.setBackgroundColor(backColor)
        }

        nameTextView!!.text = unsplashImage.userName
        progressView!!.setProgress(5)

        val backColor = unsplashImage.themeColor
        if (!ColorUtil.isColorLight(backColor)) {
            nameTextView?.setTextColor(Color.WHITE)
            lineView?.background = ColorDrawable(Color.WHITE)
            photoByTextView?.setTextColor(Color.WHITE)
        } else {
            nameTextView?.setTextColor(Color.BLACK)
            lineView?.background = ColorDrawable(Color.BLACK)
            photoByTextView?.setTextColor(Color.BLACK)
        }

        heroDV?.setImageURI(unsplashImage.listUrl)
        detailRootScrollView!!.visibility = View.VISIBLE

        val heroImagePosition = IntArray(2)
        detailImgRL?.getLocationOnScreen(heroImagePosition)

        val itemY = rectF.top.toInt()

        associatedDownloadItem = DownloadUtil.getDownloadItemById(unsplashImage?.id!!)
        if (associatedDownloadItem != null) {
            Log.d(TAG, "found down item,status:" + associatedDownloadItem!!.status)
            when (associatedDownloadItem?.status) {
                DownloadItem.DOWNLOAD_STATUS_DOWNLOADING -> {
                    downloadFlipperView?.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOADING)
                    progressView?.setProgress(associatedDownloadItem!!.progress)
                }
                DownloadItem.DOWNLOAD_STATUS_FAILED -> {
                }
                DownloadItem.DOWNLOAD_STATUS_OK -> if (clickedImage!!.hasDownloaded()) {
                    downloadFlipperView?.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD_OK)
                }
            }
            associateWithDownloadItem(associatedDownloadItem)
        }

        val targetPositionY = targetY

        checkDownloadStatus()

        toggleMaskAnimation(true)
        toggleHeroViewAnimation(itemY, targetPositionY, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receivedDownloadStarted(event: DownloadStartedEvent) {
        if (clickedImage != null && event.id == clickedImage?.id) {
            downloadFlipperView?.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOADING)
            associateWithDownloadItem(null)
        }
    }

    interface StateListener {
        fun onShowing()

        fun onHiding()

        fun onShown()

        fun onHidden()
    }

    companion object {
        private val TAG = "ImageDetailView"
        private val RESULT_CODE = 10000
        private val SHARE_TEXT = "Share %s's amazing photo from MyerSplash app. Download this photo: %s"

        private val DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD = 0
        private val DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOADING = 1
        private val DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD_OK = 2
    }
}
