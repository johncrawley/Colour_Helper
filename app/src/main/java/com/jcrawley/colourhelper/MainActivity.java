package com.jcrawley.colourhelper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private ImageView srcImageView;
    private TextView rgbTextView;
    private  int[] imageViewCoordinates;
    private View selectedColorView;
    private Map<Integer, Runnable> menuActions;
    private PhotoHelper photoHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        photoHelper = new PhotoHelper(this);
        photoHelper.initActivityResultLauncherForCamera();
    }


    public void setSrcImage(BitmapDrawable bitmapDrawable){
        srcImageView.setBackground(bitmapDrawable);
    }


    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menuitems, menu);
        menuActions = new HashMap<>();
        menuActions.put(R.id.action_take_picture, () -> photoHelper.checkPermissionAndStartCamera());
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Runnable runnable = menuActions.get(item.getItemId());
        if(runnable != null) {
            runnable.run();
        }
        return true;
    }


    private void setupViews(){
        rgbTextView = findViewById(R.id.rgbText);
        srcImageView = findViewById(R.id.sourceImageView);
        selectedColorView = findViewById(R.id.selectedColorView);
        setupImageViewListenersAfterLayoutHasBeenLoaded();
        rgbTextView.setOnClickListener(v -> copyToClipBoard());
    }


    private void setupImageViewListenersAfterLayoutHasBeenLoaded(){
        ViewGroup layout = findViewById(R.id.mainLayout);
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Drawable imgDrawable = srcImageView.getDrawable();
                Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();
                imageViewCoordinates = getStartCoordinates();
                Point p = getScaledImageDimensions(srcImageView);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, p.x, p.y, false);
                srcImageView.setOnTouchListener((view, motionEvent) -> setColorRgbTextFromImagePixel(motionEvent, scaledBitmap));
            }
        });
    }


    private boolean setColorRgbTextFromImagePixel(MotionEvent motionEvent, Bitmap scaledBitmap){
        int x = getCoordinate(motionEvent.getX(), scaledBitmap.getWidth(), imageViewCoordinates[0]);
        int y = getCoordinate(motionEvent.getY(), scaledBitmap.getHeight(), imageViewCoordinates[1]);
        int pixelColorValue = scaledBitmap.getPixel(x, y);
        String colorText = createRgbStr(pixelColorValue);
        rgbTextView.setText(colorText);
        selectedColorView.setBackgroundColor(pixelColorValue);
        return true;
    }


    private int getCoordinate(float motionEventCoordinate, int maxValue, int imageViewCoordinate){
        float coordinate = motionEventCoordinate - imageViewCoordinate;
        return getLimitedCoordinate((int)coordinate, maxValue - 1);
    }


    public int[] getStartCoordinates(){
        int[] startCoordinates = new int[2];
        srcImageView.getLocationOnScreen(startCoordinates);
        return startCoordinates;
    }


    private String createRgbStr(int pixelValue){
        String str = Integer.toHexString(pixelValue).substring(2);
        return "#" + str;
    }


    private Point getScaledImageDimensions(ImageView imageView){
        float actualHeight, actualWidth;
        float viewHeight = imageView.getHeight();
        float viewWidth = imageView.getWidth();
        float bitmapHeight = 500, bitmapWidth = 500;

        if (viewHeight * bitmapWidth <= viewWidth * bitmapHeight) {
            actualWidth = bitmapWidth * viewHeight / bitmapHeight;
            actualHeight = viewHeight;
        } else {
            actualHeight = bitmapHeight * viewWidth / bitmapWidth;
            actualWidth = viewWidth;
        }
        return new Point((int)actualWidth, (int)actualHeight);
    }


    private int getLimitedCoordinate(int coordinate, int maxLimit){
        return Math.min(Math.max(coordinate,0), maxLimit);
    }


    private void copyToClipBoard(){
        String text = rgbTextView.getText().toString().trim();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("colour", text);
        clipboard.setPrimaryClip(clip);
        //clipboard.getPrimaryClip().getItemAt(0);
        Toast.makeText(this, getString(R.string.copied_to_clipboard_toast), Toast.LENGTH_SHORT).show();
    }



}
