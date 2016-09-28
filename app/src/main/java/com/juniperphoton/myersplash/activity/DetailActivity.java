package com.juniperphoton.myersplash.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.ColorUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @Bind(R.id.activity_detail_iv)
    SimpleDraweeView mHeroImageView;

    @Bind(R.id.activity_detail_rl)
    RelativeLayout mDetailRootRelativeLayout;

    @Bind(R.id.activity_detail_name_tv)
    TextView mNameTextView;

    @Bind(R.id.activity_detail_photoby_tv)
    TextView mPhotoByTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        UnsplashImage image = (UnsplashImage) getIntent().getExtras().getSerializable("IMAGE");
        if (image != null) {
            mHeroImageView.setImageURI(image.getListUrl());

            mDetailRootRelativeLayout.setBackground(new ColorDrawable(image.getThemeColor()));
            mNameTextView.setText(image.getUserName());

            int backColor = image.getThemeColor();
            if (!ColorUtil.isColorLight(backColor)) {
                mNameTextView.setTextColor(Color.WHITE);
                mPhotoByTextView.setTextColor(Color.WHITE);
            }
        }

    }
}
