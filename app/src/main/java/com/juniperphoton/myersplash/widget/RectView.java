package com.juniperphoton.myersplash.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.base.App;

public class RectView extends View {

    public RectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(App.getInstance(), R.color.MyerSplashThemeColor));

        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }
}
