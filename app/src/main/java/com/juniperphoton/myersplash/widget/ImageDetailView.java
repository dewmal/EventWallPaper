package com.juniperphoton.myersplash.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.juniperphoton.flipperviewlib.FlipperView;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.callback.DetailViewNavigationCallback;
import com.juniperphoton.myersplash.callback.OnClickPhotoCallback;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.ColorUtil;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.ToastService;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("UnusedDeclaration")
public class ImageDetailView extends FrameLayout implements OnClickPhotoCallback {
    private static final String TAG = ImageDetailView.class.getName();
    private static final int RESULT_CODE = 10000;
    private static final String SHARE_TEXT = "Share %s's amazing photo from MyerSplash app. Download this photo: %s";

    private Context mContext;

    private File mCopyFileForSharing;

    private int mHeroStartY = 0;
    private int mHeroEndY = 0;

    private View mClickedView;
    private UnsplashImage mClickedImage;

    private DetailViewNavigationCallback mNavigationCallback;

    @BindView(R.id.detail_root_sv)
    ScrollView mDetailRootScrollView;

    @BindView(R.id.detail_hero_dv)
    SimpleDraweeView mHeroDV;

    @BindView(R.id.detail_backgrd_rl)
    RelativeLayout mDetailInfoRootLayout;

    @BindView(R.id.detail_img_rl)
    RelativeLayout mDetailImgRL;

    @BindView(R.id.detail_name_tv)
    TextView mNameTextView;

    @BindView(R.id.detail_name_line)
    View mLineView;

    @BindView(R.id.detail_photo_by_tv)
    TextView mPhotoByTextView;

    @BindView(R.id.detail_download_fab)
    FloatingActionButton mDownloadFAB;

    @BindView(R.id.detail_cancel_download_fab)
    FloatingActionButton mCancelDownloadFAB;

    @BindView(R.id.detail_share_fab)
    FloatingActionButton mShareFAB;

    @BindView(R.id.copy_url_tv)
    TextView mCopyUrlTextView;

    @BindView(R.id.copied_url_tv)
    TextView mCopiedUrlTextView;

    @BindView(R.id.copy_url_fl)
    FrameLayout mCopyLayout;

    @BindView(R.id.copied_url_fl)
    FrameLayout mCopiedLayout;

    @BindView(R.id.copy_url_flipper_view)
    FlipperView mCopyUrlFlipperView;

    @BindView(R.id.download_flipper_view)
    FlipperView mDownloadFlipperView;

//    @BindView(R.id.detail_progress_ring)
//    RingProgressView mProgressView;

    private boolean mAnimating;
    private boolean mCopied;

    public ImageDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.detail_content, this, true);
        ButterKnife.bind(this, this);

        initDetailViews();
    }

    @OnClick(R.id.detail_name_tv)
    void onClickName() {
        Uri uri = Uri.parse(mClickedImage.getUserHomePage());

        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

        intentBuilder.setToolbarColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));

        intentBuilder.setStartAnimations(mContext, R.anim.in_from_right, R.anim.out_from_left);
        intentBuilder.setExitAnimations(mContext, R.anim.in_from_left, R.anim.out_from_right);

        CustomTabsIntent customTabsIntent = intentBuilder.build();

        customTabsIntent.launchUrl(mContext, uri);
    }

    @OnClick(R.id.detail_download_fab)
    void onClickDownload() {
        if (mClickedImage == null) {
            return;
        }
        mDownloadFlipperView.next(1);
        DownloadUtil.checkAndDownload((Activity) mContext, mClickedImage);
    }

    @OnClick(R.id.detail_cancel_download_fab)
    void onClickCancel() {
        if (mClickedImage == null) {
            return;
        }
        mDownloadFlipperView.next(0);
    }

    @OnClick(R.id.copy_url_flipper_view)
    void onClickCopy() {
        if (mCopied) return;
        mCopied = true;

        mCopyUrlFlipperView.next();

        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(mContext.getString(R.string.app_name), mClickedImage.getDownloadUrl());
        clipboard.setPrimaryClip(clip);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                mCopyUrlFlipperView.next();
                mCopied = false;
            }
        }, 2000);
    }

    @OnClick(R.id.detail_share_fab)
    void onClickShare() {
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(
                ImageRequest.fromUri(Uri.parse(mClickedImage.getListUrl())), null);

        File localFile = null;

        if (cacheKey != null) {
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }

        boolean copied = false;
        if (localFile != null && localFile.exists()) {
            mCopyFileForSharing = new File(DownloadUtil.getGalleryPath(), "Share-" + localFile.getName());
            copied = DownloadUtil.copyFile(localFile, mCopyFileForSharing);
        }

        if (mCopyFileForSharing == null || !mCopyFileForSharing.exists() || !copied) {
            ToastService.sendShortToast(mContext.getString(R.string.something_wrong));
            return;
        }

        String shareText = String.format(SHARE_TEXT, mClickedImage.getUserName(), mClickedImage.getDownloadUrl());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/jpg");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mCopyFileForSharing));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ((Activity) mContext).startActivityForResult(Intent.createChooser(intent, "Share"), RESULT_CODE, null);
    }

    private void initDetailViews() {
        mDetailRootScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                        tryHide();
                    }
                    break;
                }
                return true;
            }
        });

        mDetailRootScrollView.setVisibility(View.INVISIBLE);

        mDetailInfoRootLayout.setTranslationY(-getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height));
        mDownloadFlipperView.setTranslationX(getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide));
        mShareFAB.setTranslationX(getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right_hide));

//        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 360);
//        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                mProgressView.setRotation((float) animation.getAnimatedValue());
//            }
//        });
//        valueAnimator.setDuration(300);
//        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
//        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        valueAnimator.start();
    }

    private void toggleHeroViewAnimation(int startY, int endY, final boolean show) {
        if (show) {
            mHeroStartY = startY;
            mHeroEndY = endY;
        } else {
            mDownloadFlipperView.next(0);
        }

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(startY, endY);
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDetailImgRL.getLayoutParams());
                params.setMargins(0, (int) animation.getAnimatedValue(), 0, 0);
                mDetailImgRL.setLayoutParams(params);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show && mClickedView != null) {
                    mClickedView.setVisibility(View.VISIBLE);
                    toggleMaskAnimation(false);
                    mClickedView = null;
                    mClickedImage = null;
                    mAnimating = false;
                } else {
                    toggleDetailRLAnimation(true);
                    toggleDownloadBtnAnimation(true);
                    toggleShareBtnAnimation(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    private int getTargetY() {
        return (((Activity) mContext).getWindow().getDecorView().getHeight() -
                (getResources().getDimensionPixelSize(R.dimen.img_detail_height))) / 2;
    }

    private void toggleDetailRLAnimation(final boolean show) {
        int startY = show ? (-getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height)) : 0;
        int endY = show ? 0 : (-getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height));

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(startY, endY);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDetailInfoRootLayout.setTranslationY((int) animation.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    toggleHeroViewAnimation(mHeroEndY, mHeroStartY, false);
                } else {
                    mAnimating = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    private void toggleDownloadBtnAnimation(final boolean show) {
        int normalX = getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_right);

        int hideX = getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(show ? hideX : 0, show ? 0 : hideX);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDownloadFlipperView.setTranslationX((int) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    private void toggleShareBtnAnimation(final boolean show) {
        int normalX = getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right);

        int hideX = getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right_hide);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(show ? hideX : 0, show ? 0 : hideX);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mShareFAB.setTranslationX((int) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    private void toggleMaskAnimation(final boolean show) {
        ValueAnimator animator = ValueAnimator.ofArgb(show ? Color.TRANSPARENT : ContextCompat.getColor(mContext, R.color.MaskColor),
                show ? ContextCompat.getColor(mContext, R.color.MaskColor) : Color.TRANSPARENT);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDetailRootScrollView.setBackground(new ColorDrawable((int) animation.getAnimatedValue()));
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (show) {
                    mNavigationCallback.onShow();
                } else {
                    mNavigationCallback.onHide();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (show) {
                    mNavigationCallback.onShown();
                } else {
                    mDetailRootScrollView.setVisibility(View.INVISIBLE);
                    if (mNavigationCallback != null) {
                        mNavigationCallback.onHidden();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void hideDetailPanel() {
        if (mAnimating) return;
        toggleDetailRLAnimation(false);
        toggleDownloadBtnAnimation(false);
        toggleShareBtnAnimation(false);
    }

    public void setNavigationCallback(DetailViewNavigationCallback callback) {
        mNavigationCallback = callback;
    }

    public void deleteShareFileInDelay() {
        //TODO: Should has a better way to do this.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCopyFileForSharing != null && mCopyFileForSharing.exists()) {
                    boolean ok = mCopyFileForSharing.delete();
                }
            }
        }, 15000);
    }

    public boolean tryHide() {
        if (mDetailRootScrollView.getVisibility() == View.VISIBLE) {
            hideDetailPanel();
            return true;
        }
        return false;
    }

    @Override
    public void clickPhotoItem(final RectF rectF, final UnsplashImage unsplashImage, View itemView) {
        if (mClickedView != null) {
            return;
        }
        mClickedImage = unsplashImage;
        mClickedView = itemView;
        mClickedView.setVisibility(View.INVISIBLE);

        mDetailInfoRootLayout.setBackground(new ColorDrawable(unsplashImage.getThemeColor()));
        int themeColor = unsplashImage.getThemeColor();
        int alpha = Color.alpha(themeColor);

        //Dark
        if (!ColorUtil.isColorLight(themeColor)) {
            mCopyUrlTextView.setTextColor(Color.BLACK);
            int backColor = Color.argb(255, Color.red(Color.WHITE),
                    Color.green(Color.WHITE), Color.blue(Color.WHITE));
            mCopyLayout.setBackgroundColor(backColor);
        } else {
            mCopyUrlTextView.setTextColor(Color.WHITE);
            int backColor = Color.argb(255, Color.red(Color.BLACK),
                    Color.green(Color.BLACK), Color.blue(Color.BLACK));
            mCopyLayout.setBackgroundColor(backColor);
        }

        mNameTextView.setText(unsplashImage.getUserName());

        int backColor = unsplashImage.getThemeColor();
        if (!ColorUtil.isColorLight(backColor)) {
            mNameTextView.setTextColor(Color.WHITE);
            mLineView.setBackground(new ColorDrawable(Color.WHITE));
            mPhotoByTextView.setTextColor(Color.WHITE);
        } else {
            mNameTextView.setTextColor(Color.BLACK);
            mLineView.setBackground(new ColorDrawable(Color.BLACK));
            mPhotoByTextView.setTextColor(Color.BLACK);
        }

        mHeroDV.setImageURI(unsplashImage.getListUrl());
        mDetailRootScrollView.setVisibility(View.VISIBLE);

        int[] heroImagePosition = new int[2];
        mDetailImgRL.getLocationOnScreen(heroImagePosition);

        int itemY = (int) rectF.top;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDetailImgRL.getLayoutParams());
        params.setMargins(0, itemY - (int) (20 * 3.5), 0, 0);

        mDetailImgRL.setLayoutParams(params);

        int targetPositionY = getTargetY();

        toggleMaskAnimation(true);
        toggleHeroViewAnimation(itemY, targetPositionY, true);
    }
}
