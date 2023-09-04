package com.jcrawley.colourhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

    private Matrix invertMatrix;

    @SuppressLint("ClickableViewAccessibility")
    private void setupViews(){

        rgbTextView = findViewById(R.id.rgbText);
        srcImageView = findViewById(R.id.sourceImageView);
        invertMatrix = new Matrix();
        srcImageView.getImageMatrix().invert(invertMatrix);
        srcImageView.setOnTouchListener((view, motionEvent) -> {
            float[] invertedPoints = getCoordinatesFromInverseMatrix(motionEvent.getX(), motionEvent.getY());
            int x = (int) invertedPoints[0];
            int y = (int) invertedPoints[1];

            float startX = srcImageView.getX();
            float startY = srcImageView.getY();

            Drawable imgDrawable = ((ImageView)view).getDrawable();
            Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();

            float picX = motionEvent.getX() - startX;
            float picY = motionEvent.getY() - startY;


            x = getLimitedCoordinate((int)picX, bitmap.getWidth() - 1);
            y = getLimitedCoordinate((int)picY, bitmap.getHeight() -1);

            int touchedRGB = bitmap.getPixel(x, y);

            String colorText = "#" + Integer.toHexString(touchedRGB) + " startX,Y: " + startX + ","  + startY + ",  motion X,Y: " + x+ "," +y;
            rgbTextView.setText(colorText);
            rgbTextView.setTextColor(touchedRGB);
            return true;
        });


    };


    private float[] getCoordinatesFromInverseMatrix(float x, float y){
        float[] eventXY = new float[] {x, y};
        invertMatrix.mapPoints(eventXY);
        return eventXY;
    }


    int getLimitedCoordinate(int coordinate, int maxLimit){
        return Math.min(Math.max(coordinate,0), maxLimit);
    }
}
