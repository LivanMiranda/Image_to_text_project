package com.example.imagetotextproject;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private MaterialButton ImageButton;
    private MaterialButton recognizedBtn;
    private ShapeableImageView imageView;
    private EditText recognizedText;
    
    private static final String TAG = "Main";
    private Uri imageUri = null;
    private static final int CAMERA_Request_CODE = 100;
    private static final int STORAGE_Request_CODE = 101;
    private String[] cameraPermission;
    private String[] storagePermission;

    private ProgressDialog dialog;
    private TextRecognizer recognizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton = findViewById(R.id.ImageButton);
        recognizedBtn = findViewById(R.id.recognizedBtn);
        imageView = findViewById(R.id.imageView);
        recognizedText = findViewById(R.id.recognizedText);

        cameraPermission = new String[]{
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        dialog = new ProgressDialog(this);
        dialog.setTitle("Wait");
        dialog.setCanceledOnTouchOutside(false);

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        ImageButton.setOnClickListener(v -> showInputImageDialog());

        recognizedBtn.setOnClickListener(v -> {

            if (imageUri == null){
                Toast.makeText(MainActivity.this, "Pick image", Toast.LENGTH_SHORT).show();
            }
            else{

                recognizeTextFromImage();
            }

        });

    }

    private void recognizeTextFromImage() {
        Log.d(TAG, "recognizeTextFromImage: ");
        dialog.setMessage("Reading the image");
        dialog.show();
try {
    InputImage image = InputImage.fromFilePath(this , imageUri);
    dialog.setMessage("Converting to text");

    Task<Text> testResult = recognizer.process(image).addOnSuccessListener((Text text) -> {

        dialog.dismiss();

      String recognizedText1 = text.getText();
        ArrayList<String> x = new ArrayList<>();
        Scanner scanner = new Scanner(recognizedText1);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            x.add(line);
        }
        Double tipOut = Double.parseDouble(x.get(x.size()-1))*0.075;
        scanner.close();
        Log.d(TAG, "onSuccess: recognizedText1 " + recognizedText1);
     recognizedText.setText(new StringBuilder().append(" You own the support staff $").append(tipOut).append(" dollars").toString());
    }).addOnFailureListener(e -> {

        dialog.dismiss();
        Log.d(TAG, "onFailure: ", e);
        Toast.makeText(MainActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();

    });

}catch (Exception e){
    dialog.dismiss();
    Toast.makeText(MainActivity.this, "Error ", Toast.LENGTH_SHORT).show();

}

    }

    private void showInputImageDialog() {
        PopupMenu menu = new PopupMenu(this, ImageButton);

        menu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        menu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        menu.show();
        menu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1){
                Log.d(TAG, "onMenuItemClick: camera clicked...");
                if(checkCameraPermission()){
                    pickImageCamera();
                }
                else{
                    requestCameraPermission();
                }
            }
            else if (id == 2){
                Log.d(TAG, "onMenuItemClick: Gallery clicked...");
              if(checkStoragePermission()){
                  PickImageGallery();
              }
              else{
                  requestStoragePermission();
              }
            }
            return true;
        });
    }

    private void PickImageGallery(){
        Log.d(TAG, "PickImageGallery: ");

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){

                Intent data = result.getData();
                assert data != null;
                imageUri = data.getData();
                Log.d(TAG, "onActivityResult: imageUri " + imageUri);
                imageView.setImageURI(imageUri);
            }
            else{
                Log.d(TAG, "onActivityResult: Cancelled");
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
            }
        }
    );

    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Title");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }
    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: imageUri" + imageUri);
                        imageView.setImageURI(imageUri);
                    }
                    else{
                        Log.d(TAG, "onActivityResult: cancelled");
                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkStoragePermission(){

        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission(){

        ActivityCompat.requestPermissions( this, storagePermission, STORAGE_Request_CODE);
    }

    private boolean checkCameraPermission(){

        boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return cameraResult && storageResult;
    }

    private void requestCameraPermission(){

        ActivityCompat.requestPermissions( this, cameraPermission, CAMERA_Request_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_Request_CODE:{

                if (grantResults.length>0){

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted){
                        pickImageCamera();
                    }
                    else{

                        Toast.makeText( this, "Permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText( this, "Cancelled", Toast.LENGTH_SHORT).show();

                }
            }
            break;
            case STORAGE_Request_CODE:{

                if (grantResults.length>0){

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if( storageAccepted){
                        PickImageGallery();
                    }
                    else{

                        Toast.makeText( this, "Permissions are required", Toast.LENGTH_SHORT).show();

                    }
                }

            }
            break;

        }
    }
}