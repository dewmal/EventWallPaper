package com.juniperphoton.myersplash.utils;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.base.App;

public class ToastService {
    public static void sendShortToast(String str) {
        LayoutInflater inflater = LayoutInflater.from(App.getInstance());
        View view = inflater.inflate(R.layout.toast_layout, null);

        TextView textView = (TextView) view.findViewById(R.id.toast_textView);
        textView.setText(str);

        Toast toast = new Toast(App.getInstance());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.show();
    }
}