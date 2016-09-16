package com.juniperphoton.myersplash.model;

import java.io.Serializable;

public class UnsplashUser implements Serializable {
    private String id;
    private String username;
    private String name;

    public UnsplashUser(){

    }

    public String getName(){
        return name;
    }
}
