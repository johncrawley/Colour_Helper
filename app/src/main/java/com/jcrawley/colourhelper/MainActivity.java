package com.jcrawley.colourhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ImageView srcImageView;
    private TextView rgbTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
    }

    private void log(String msg){
        System.out.println("^^^ MainActivity: " + msg);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupViews(){

        rgbTextView = findViewById(R.id.rgbText);
        srcImageView = findViewById(R.id.sourceImageView);
        ViewGroup layout = findViewById(R.id.mainLayout);
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Drawable imgDrawable = srcImageView.getDrawable();
                Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();
                int imageHeight = srcImageView.getDrawable().getIntrinsicHeight();
                int imageWidth = srcImageView.getDrawable().getIntrinsicWidth();

                Point p = getScaledImageDimensions(srcImageView);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, p.x, p.y, false);

                log("srcImageView width, height: " + srcImageView.getMeasuredWidth() + "," + srcImageView.getMeasuredHeight());
                log("scaledBitmap width, height: " + p.x + "," + p.y);


                srcImageView.setOnTouchListener((view, motionEvent) -> {
                    int[] viewLocation = new int[2];
                    srcImageView.getLocationOnScreen(viewLocation);
                    float startX = viewLocation[0];
                    float startY = viewLocation[1];

                    float picX = motionEvent.getX() - startX;
                    float picY = motionEvent.getY() - startY;

                    int x = getLimitedCoordinate((int)picX, scaledBitmap.getWidth() - 1);
                    int y = getLimitedCoordinate((int)picY, scaledBitmap.getHeight() -1);

                    int touchedRGB = scaledBitmap.getPixel(x, y);

                    String colorText = "#" + Integer.toHexString(touchedRGB);
                    rgbTextView.setText(colorText);
                    rgbTextView.setTextColor(touchedRGB);
                    return true;
                });
            }
        });

        rgbTextView.setOnClickListener(v -> copyToClipBoard());

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
    }


}
