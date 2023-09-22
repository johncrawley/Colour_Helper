package com.jcrawley.colourhelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private ImageView srcImageView;
    private TextView rgbTextView;
    private  int[] imageViewCoordinates;
    private View selectedColorView;
    private Map<Integer, Runnable> menuActions;
    private PhotoHelper photoHelper;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViewModel();
        setupViews();
        photoHelper = new PhotoHelper(this);
        photoHelper.initActivityResultLauncherForCamera();
    }

    private void setupViewModel(){
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }


    public void setSrcImage(Bitmap bitmap){
        srcImageView.setImageBitmap(bitmap);
        setupScaledImage();
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
        setupImageViewListenersAfterLayoutLoads();
        rgbTextView.setOnClickListener(v -> copyToClipBoard());
    }


    private void setupImageViewListenersAfterLayoutLoads(){
        ViewGroup layout = findViewById(R.id.mainLayout);
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if(!viewModel.isImageAssigned){
                    setupScaledImage();
                }
                srcImageView.setOnTouchListener((view, motionEvent) -> setColorRgbTextFromImagePixel(motionEvent));
            }
        });
    }


    private void setupScaledImage(){
        viewModel.imageViewWidth = srcImageView.getMeasuredWidth();
        viewModel.imageViewHeight = srcImageView.getMeasuredHeight();
        Drawable imgDrawable = srcImageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();
        imageViewCoordinates = getImageViewCoordinates();
        viewModel.imageViewRect.top = 0;
        viewModel.imageViewRect.left = imageViewCoordinates[0];
        viewModel.imageViewRect.bottom = viewModel.imageViewRect.top + viewModel.imageViewHeight;
        viewModel.imageViewRect.right = viewModel.imageViewRect.left + viewModel.imageViewWidth;
        setupScaledBitmap(bitmap);
    }


    private void setupScaledBitmap(Bitmap bitmap){
        viewModel.scaledBitmap = Bitmap.createScaledBitmap(bitmap, viewModel.imageViewWidth, viewModel.imageViewHeight, false);
    }


    private boolean setColorRgbTextFromImagePixel(MotionEvent motionEvent){
        if(isSelectedPointOutsideImageBounds(motionEvent)){
            return true;
        }
        int x = getCoordinate(motionEvent.getX(), viewModel.scaledBitmap.getWidth(), imageViewCoordinates[0]);
        int y = getCoordinate(motionEvent.getY(), viewModel.scaledBitmap.getHeight(), 0);
        int pixelColorValue = viewModel.scaledBitmap.getPixel(x, y);
        String colorText = createRgbStr(pixelColorValue);
        rgbTextView.setText(colorText);
        selectedColorView.setBackgroundColor(pixelColorValue);
        return true;
    }


    private boolean isSelectedPointOutsideImageBounds(MotionEvent motionEvent){
        int motionX = (int)motionEvent.getX();
        int motionY = (int)motionEvent.getY();
        return !viewModel.imageViewRect.contains(motionX, motionY);
    }


    private int getCoordinate(float motionEventCoordinate, int maxValue, int imageViewCoordinate){
        float coordinate = motionEventCoordinate - imageViewCoordinate;
        return getLimitedCoordinate((int)coordinate, maxValue - 1);
    }


    private int[] getImageViewCoordinates(){
        int[] startCoordinates = new int[2];
        srcImageView.getLocationOnScreen(startCoordinates);
        return startCoordinates;
    }


    private String createRgbStr(int pixelValue){
        String str = Integer.toHexString(pixelValue).substring(2);
        return "#" + str;
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
