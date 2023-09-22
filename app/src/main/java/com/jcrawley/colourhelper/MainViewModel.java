package com.jcrawley.colourhelper;

import android.graphics.Bitmap;
import android.graphics.Rect;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {


    public Bitmap scaledBitmap;
    public int imageViewWidth, imageViewHeight;
    public Rect imageViewRect = new Rect();
    public boolean isImageAssigned;
}
