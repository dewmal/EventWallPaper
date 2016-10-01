package com.juniperphoton.myersplash.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
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
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.callback.DetailViewNavigationCallback;
import com.juniperphoton.myersplash.callback.OnClickPhotoCallback;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.ColorUtil;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.ToastService;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("UnusedDeclaration")
public class DetailView extends FrameLayout implements OnClickPhotoCallback {

    private static final String TAG = DetailView.class.getName();
    private static final int RESULT_CODE = 10000;
    private static final String SHARE_TEXT = "Share %s's amazing photo from MyerSplash app. %s";

    private Context mContext;

    private File mCopyFileForSharing;

    private int mHeroStartY = 0;
    private int mHeroEndY = 0;

    private View mClickedView;
    private UnsplashImage mClickedImage;

    private DetailViewNavigationCallback mNavigationCallback;

    @Bind(R.id.detail_root_sv)
    ScrollView mDetailRootScrollView;

    @Bind(R.id.detail_hero_dv)
    SimpleDraweeView mHeroDV;

    @Bind(R.id.detail_backgrd_rl)
    RelativeLayout mDetailInfoRootLayout;

    @Bind(R.id.detail_img_rl)
    RelativeLayout mDetailImgRL;

    @Bind(R.id.detail_name_tv)
    TextView mNameTextView;

    @Bind(R.id.detail_photoby_tv)
    TextView mPhotoByTextView;

    @Bind(R.id.detail_download_fab)
    FloatingActionButton mDownloadFAB;

    @Bind(R.id.detail_share_fab)
    FloatingActionButton mShareFAB;

    public DetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.detail_content, this);
        ButterKnife.bind(this);

        initDetailViews();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.detail_download_fab)
    void onClickDownload() {
        if (mClickedImage == null) {
            return;
        }
        DownloadUtil.checkAndDownload((Activity) mContext, mClickedImage.getFileNameForDownload(),
                mClickedImage.getDownloadUrl());
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.detail_root_sv)
    void onClickMaskSV() {
        hideDetailPanel();
    }

    @SuppressWarnings("UnusedDeclaration")
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
            ToastService.sendShortToast("Something went wrong :-(");
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
                return false;
            }
        });

        mDetailRootScrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailView.this.hideDetailPanel();
            }
        });

        mDetailRootScrollView.setVisibility(View.INVISIBLE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDetailInfoRootLayout.getLayoutParams());
        params.setMargins(0, 0, 0,
                getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mDetailInfoRootLayout.setLayoutParams(params);

        RelativeLayout.LayoutParams paramsDFAB = new RelativeLayout.LayoutParams(mDownloadFAB.getLayoutParams());
        paramsDFAB.setMargins(0, 0, getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide),
                getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_bottom));
        paramsDFAB.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        paramsDFAB.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mDownloadFAB.setLayoutParams(paramsDFAB);

        RelativeLayout.LayoutParams paramsSFAB = new RelativeLayout.LayoutParams(mDownloadFAB.getLayoutParams());
        paramsSFAB.setMargins(0, 0, getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right),
                getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_bottom));
        paramsSFAB.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        paramsSFAB.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mShareFAB.setLayoutParams(paramsSFAB);
    }

    private void toggleHeroViewAnimation(int startY, int endY, final boolean show) {
        if (show) {
            mHeroStartY = startY;
            mHeroEndY = endY;
        }

        //Logger.d("start:" + startY + ",end:" + endY);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(startY, endY);
        valueAnimator.setDuration(300);
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
                } else {
                    toggleDetailRLAnimation(true);
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

        int startY = show ? (getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height)) : 0;
        int endY = show ? 0 : (getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height));

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(startY, endY);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDetailInfoRootLayout.getLayoutParams());
                params.setMargins(0, 0, 0,
                        (int) animation.getAnimatedValue());
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                mDetailInfoRootLayout.setLayoutParams(params);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    toggleHeroViewAnimation(mHeroEndY, mHeroStartY, false);
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
        valueAnimator.setIntValues(show ? hideX : normalX, show ? normalX : hideX);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams paramsFAB = new RelativeLayout.LayoutParams(mDownloadFAB.getLayoutParams());
                paramsFAB.setMargins(0, 0, (int) animation.getAnimatedValue(),
                        getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_bottom));
                paramsFAB.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                paramsFAB.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                mDownloadFAB.setLayoutParams(paramsFAB);
            }
        });
        valueAnimator.start();
    }

    private void toggleShareBtnAnimation(final boolean show) {
        int normalX = getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right);

        int hideX = getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right_hide);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(show ? hideX : normalX, show ? normalX : hideX);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams paramsFAB = new RelativeLayout.LayoutParams(mShareFAB.getLayoutParams());
                paramsFAB.setMargins(0, 0, (int) animation.getAnimatedValue(),
                        getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_bottom));
                paramsFAB.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                paramsFAB.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                mShareFAB.setLayoutParams(paramsFAB);
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
        mNameTextView.setText(unsplashImage.getUserName());

        int backColor = unsplashImage.getThemeColor();
        if (!ColorUtil.isColorLight(backColor)) {
            mNameTextView.setTextColor(Color.WHITE);
            mPhotoByTextView.setTextColor(Color.WHITE);
        } else {
            mNameTextView.setTextColor(Color.BLACK);
            mPhotoByTextView.setTextColor(Color.BLACK);
        }

        mHeroDV.setImageURI(unsplashImage.getListUrl());
        mDetailRootScrollView.setVisibility(View.VISIBLE);

        int[] heroImgePosition = new int[2];
        mDetailImgRL.getLocationOnScreen(heroImgePosition);

        int itemY = (int) rectF.top;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDetailImgRL.getLayoutParams());
        params.setMargins(0, itemY - (int) (20 * 3.5), 0, 0);

        mDetailImgRL.setLayoutParams(params);

        int targetPositonY = getTargetY();

        toggleMaskAnimation(true);
        toggleHeroViewAnimation(itemY, targetPositonY, true);
        toggleDownloadBtnAnimation(true);
        toggleShareBtnAnimation(true);
    }
}
