package com.juniperphoton.myersplash.activity;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.CategoryAdapter;
import com.juniperphoton.myersplash.callback.INavigationDrawerCallback;
import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.model.UnsplashCategory;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import moe.feng.material.statusbar.StatusBarCompat;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements INavigationDrawerCallback {

    @Bind(R.id.activity_drawer_rv)
    RecyclerView mDrawerRecyclerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            drawer.post(new Runnable() {
                @Override
                public void run() {
                    toggle.syncState();
                }
            });
        }

        ButterKnife.bind(this);
        mDrawerRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        getCategories();
    }

    //进行网络请求
    private void getCategories() {
        CloudService.getInstance().getCategories(new Subscriber<List<UnsplashCategory>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<UnsplashCategory> unsplashCategories) {
                UnsplashCategory newCategory = new UnsplashCategory();
                newCategory.setId(-1);
                newCategory.setTitle("New");
                UnsplashCategory featureCategory = new UnsplashCategory();
                featureCategory.setId(-2);
                featureCategory.setTitle("Featured");
                unsplashCategories.add(0, featureCategory);
                unsplashCategories.add(0, newCategory);
                CategoryAdapter adapter = new CategoryAdapter(unsplashCategories, MainActivity.this);
                adapter.setCallback(MainActivity.this);
                mDrawerRecyclerview.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void selectItem(UnsplashCategory category) {

    }
}
