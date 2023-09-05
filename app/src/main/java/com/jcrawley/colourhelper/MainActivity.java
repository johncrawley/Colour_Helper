package com.jcrawley.colourhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, srcImageView.getMeasuredWidth(), srcImageView.getMeasuredHeight(), false);


                System.out.println("^^^ srcImageView width, height: " + srcImageView.getWidth() + "," + srcImageView.getHeight());
                System.out.println("^^^ ^scaledBitmap width, height: " + scaledBitmap.getWidth() + "," + scaledBitmap.getHeight());


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

                    String colorText = "#" + Integer.toHexString(touchedRGB) + " startX,Y: " + startX + ","  + startY + ",  motion X,Y: " + x+ "," +y;
                    rgbTextView.setText(colorText);
                    rgbTextView.setTextColor(touchedRGB);
                    return true;
                });
            }
        });



    };


    int getLimitedCoordinate(int coordinate, int maxLimit){
        return Math.min(Math.max(coordinate,0), maxLimit);
    }
}
