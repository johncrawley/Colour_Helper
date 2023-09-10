package com.jcrawley.colourhelper;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PhotoHelper {

    private final MainActivity mainActivity;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private String currentPhotoPath;

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
                        loadImage();
                    }
                });
    }


    private void startTakePictureActivity(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createTempImageFile();
        if (photoFile == null) {
            Toast.makeText(mainActivity, "unable to create temp image file", Toast.LENGTH_SHORT).show();
            System.out.println("^^^ unable to create temp image file");
            return;
        }
        Uri photoUri = FileProvider.getUriForFile(mainActivity, "com.jcrawley.colorpicker.android.fileprovider", photoFile);
        System.out.println("photoUri path : " + photoUri.getPath());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraActivityResultLauncher.launch(intent);
    }


    private File createTempImageFile(){
        File photoFile = null;
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
        log("createImageFile() storageDir = "  + storageDir.getAbsolutePath());
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir );
        log("temp imageFile exists? : " + imageFile.exists() + " imageFile absolute path: " + imageFile.getAbsolutePath());
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }


    public void loadImage(){
        log("Entered loadImage");
        Uri uri = Uri.parse(currentPhotoPath);
        log("loadImage() parsed Uri");
        if(uri == null){
            log("uri is null, returning");
            return;
        }
        try{
            InputStream input = mainActivity.getContentResolver().openInputStream(uri);
            log("created inputStream from contentResolver opening uri");
            if(input == null){
                log("inputStream is null, returning");
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            log("decoded input to a bitmap");
            BitmapDrawable bitmapDrawable = new BitmapDrawable(mainActivity.getResources(), bitmap);
            mainActivity.setSrcImage(bitmapDrawable);
        }catch (IOException e){
            log("IO Exception encountered");
            e.printStackTrace();
        }
    }

    private void log(String msg){
        System.out.println("^^^ PhotoHelper: " + msg);
    }
}
