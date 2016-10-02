package com.juniperphoton.myersplash.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.juniperphoton.myersplash.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moe.feng.material.statusbar.StatusBarCompat;
import retrofit2.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AboutActivity extends AppCompatActivity {

    @Bind(R.id.activity_about_backgrd_iv)
    ImageView mBannerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);
        mBannerView.setImageAlpha(0);
        setBackground();
    }

    private void setBackground() {
        Observable.create(new Observable.OnSubscribe<Drawable>() {
            @Override
            public void call(Subscriber<? super Drawable> subscriber) {
                Drawable drawable = getResources().getDrawable(R.drawable.banner, null);
                subscriber.onNext(drawable);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Drawable>() {
                    @Override
                    public void onCompleted() {
                        toggleBackgroundAlphaAnimation();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Drawable drawable) {
                        AboutActivity.this.mBannerView.setImageDrawable(drawable);
                    }
                });
    }

    private void toggleBackgroundAlphaAnimation() {
        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int alpha = (int) animation.getAnimatedValue();
                mBannerView.setImageAlpha(alpha);
            }
        });
        animator.start();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.email_rl)
    void emailClick(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dengweichao@hotmail.com"}); // recipients

        String SHARE_SUBJECT = "MyerSplash for Android %s feedback";
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(SHARE_SUBJECT, getResources().getString(R.string.Version)));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(emailIntent);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.activity_about_rate_rl)
    void rateClick(View view) {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
