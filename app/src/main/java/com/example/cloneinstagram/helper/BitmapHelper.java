package com.example.cloneinstagram.helper;

import android.graphics.Bitmap;

public class BitmapHelper {

    private Bitmap bitmap = null;

    private static final BitmapHelper instance = new BitmapHelper();
    private BitmapHelper() {}

    public static BitmapHelper getInstance() {        return instance;    }

    public Bitmap getBitmap() {        return bitmap;    }
    public void setBitmap(Bitmap bitmap) {        this.bitmap = bitmap;    }

    public  boolean clear(){
        this.bitmap = null;

        return bitmap==null;
    }
}
