package com.juniperphoton.myersplash.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;

public class SerializerUtil {
    public static String CATEGORY_LIST_FILE_NAME = "categories.list";
    public static String IMAGE_LIST_FILE_NAME = "images.list";

    public static void Cleanup(Context context) {
        File file = context.getDir(IMAGE_LIST_FILE_NAME, Context.MODE_PRIVATE);
        if (file != null) {
            file.delete();
        }
    }

    public static void serializeToFile(Context context, Object o, String fileName) {
        try {
            Gson gson = new Gson();
            String jsonString = gson.toJson(o, o.getClass());
            byte[] bytes = jsonString.getBytes();

            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T deSerializeFromFile(Type type, Context context, String fileName) {
        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int pos = 0;
            while ((pos = inputStream.read(bytes)) != -1) {
                byteArrayOutputStream.write(bytes, 0, pos);
            }

            inputStream.close();
            byteArrayOutputStream.close();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.enableComplexMapKeySerialization().create();

            return gson.fromJson(byteArrayOutputStream.toString(), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

