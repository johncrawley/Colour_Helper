package com.jcrawley.colourhelper;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class PhotoHelper {

    private final MainActivity mainActivity;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private File photoFile;
    private int initialRotation = 0;
    private float currentScale = 1;
    private int rotation = 0;


    public PhotoHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        initRequestPermissionLauncher();
    }


    public void checkPermissionAndStartCamera(){
        if (ContextCompat.checkSelfPermission(mainActivity, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startTakePictureActivity();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }


    private void initRequestPermissionLauncher(){
       requestPermissionLauncher =
        mainActivity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startTakePictureActivity();
            } else {
                Toast.makeText(mainActivity, "permission not given for camera!", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void initActivityResultLauncherForCamera(){
        cameraActivityResultLauncher = mainActivity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result != null && result.getResultCode() == RESULT_OK) {
                        //loadImage();
                        Intent data = result.getData();
                        boolean doesPhotoFileExist = photoFile != null && photoFile.exists();
                        loadImageFromPhotoFile();

                        log("camera result... photoFile still exists? : " + doesPhotoFileExist);
                        if(data == null){
                            log("loading photoBitmap but result intent is null, returning!");
                            return;
                        }
                        Bundle bundle = data.getExtras();
                        if(bundle == null){
                            log("loading photoBitmap but bundle is null, returning!");
                            return;
                        }
                        Bitmap photoBitmap = (Bitmap) bundle.get("data");
                        if(photoBitmap == null){
                            log("photoBitmap is null, returning!");
                            return;
                        }
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(mainActivity.getResources(), photoBitmap);
                        log("photo bitmap dimensions: " + photoBitmap.getHeight() + "," + photoBitmap.getWidth());
                        mainActivity.setSrcImage(photoBitmap);

                    }
                });
    }


    private void startTakePictureActivity(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createTempImageFile();
        if (photoFile == null) {
            Toast.makeText(mainActivity, "unable to create temp image file", Toast.LENGTH_SHORT).show();
            System.out.println("^^^ unable to create temp image file");
            return;
        }
        Uri photoUri = FileProvider.getUriForFile(mainActivity, "com.jcrawley.colorpicker.android.fileprovider", photoFile);


        System.out.println("photoUri path : " + photoUri.getPath());
        log("photoFile exists? : " + photoFile.exists());
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraActivityResultLauncher.launch(cameraIntent);
    }


    private File createTempImageFile(){
        photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return photoFile;
    }


    private File createImageFile() throws IOException {
        String imageFileName = "saved_photo_" + System.currentTimeMillis() + "_";
        File storageDir = mainActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir );
    }


    public void loadImageFromPhotoFile() {
        if (photoFile == null || !photoFile.exists()) {
            log("loadImageFromPhotoFile() could find temp file");
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        log("decoded input to a bitmap");
        BitmapDrawable bitmapDrawable = new BitmapDrawable(mainActivity.getResources(), bitmap);
        mainActivity.setSrcImage(createAmendedBitmapFrom(bitmap));
    }


    private int getInitialAngle(){
        if(initialRotation == 0){
            return 0;
        }
        return getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE ? 0 : 90;
    }


    private int getScreenOrientation(){
        return mainActivity.getResources().getConfiguration().orientation;
    }


    private Bitmap createAmendedBitmapFrom(Bitmap photoBitmap){
        return Bitmap.createBitmap(photoBitmap,
                0,
                0,
                photoBitmap.getWidth(),
                photoBitmap.getHeight(),
                getRotateAndScaledMatrix(true),
                true);
    }


    public Matrix getRotateAndScaledMatrix(boolean isUsingPreviewScale){
        Matrix matrix = new Matrix();
        matrix.postRotate((getInitialAngle() + rotation) % 360);
        float scale = isUsingPreviewScale ? currentScale * 2 : currentScale;
        matrix.postScale(scale, scale);
        return matrix;
    }



    private void log(String msg){
        System.out.println("^^^ PhotoHelper: " + msg);
    }
}