// ImageCaptureHelper.java
package com.example.housemanagement;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageCaptureHelper {
    ActivityResultLauncher<Intent> startActivityIntent;
    AppCompatActivity activity;
    ImageView imageView;

    String imgName;

    public ImageCaptureHelper(AppCompatActivity activity) {
        this.activity = activity;

        registerActivityResult();
    }

    private String currentPhotoPath;

    private void registerActivityResult() {
        startActivityIntent = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                // -1 is OK
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // The activity was successful
                    Log.d("a","here");
                    Intent data = result.getData();

                    //int position = Integer.parseInt(data.getStringExtra("position"));
                    Uri imageUri = data.getData();
                    if (imageUri != null) {
                        Uri copiedImageUri = copyImageToAppStorage(imageUri, imgName);

                        // Set the image URI and Tag to the URI of the copied image
                        imageView.setImageURI(copiedImageUri);
                        imageView.setTag(String.valueOf(copiedImageUri));
                    }
                }
            }
        });
    }

    public void showImageOptions(ImageView imageView, String imgName) {
        this.imageView = imageView;
        this.imgName = imgName;
        pickImageFromGallery();
//        String[] options = {"Take Photo", "Choose from Gallery"};
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setTitle("Select Option");
//
//        builder.setItems(options, (dialog, which) -> {
//            switch (which) {
//                case 0:
//                     dispatchTakePictureIntent();
//                    break;
//                case 1:
//                    pickImageFromGallery();
//                    break;
//            }
//        });
//
//        builder.show();
    }


//    private  File createImageFile() throws IOException {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        return File.createTempFile(imageFileName, ".jpg", storageDir);
//    }

//    private  void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//                currentPhotoPath = photoFile.getAbsolutePath();
//            } catch (IOException ex) {
//                // Handle error
//            }
//
//            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(activity, "com.example.housemanagement.fileprovider", photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityIntent.launch(takePictureIntent);
//            }
//        }
//    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityIntent.launch(intent);
    }

    private Uri copyImageToAppStorage(Uri sourceUri, String imgName) {
        try {
            // Open an InputStream for the source Uri
            InputStream inputStream = activity.getContentResolver().openInputStream(sourceUri);

            // Create a destination file in the app's internal storage
            imgName = imgName.replaceAll("\\s", "");
            File destinationFile = createInternalStorageFile(imgName + ".jpg");

            // Open an OutputStream for the destination file
            OutputStream outputStream = new FileOutputStream(destinationFile);

            // Copy the data from InputStream to OutputStream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close the streams
            inputStream.close();
            outputStream.close();

            // Return the URI of the copied image
            return Uri.fromFile(destinationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private File createInternalStorageFile(String fileName) {
        File internalStorageDir = activity.getFilesDir();
        return new File(internalStorageDir, fileName);
    }


    public void downloadImagesToPublicDirectoryWithConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to download all images to the public directory?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadImagesToPublicDirectory();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, user canceled the operation
            }
        });

        builder.show();
    }
    public void downloadImagesToPublicDirectory() {
        File internalStorageDir = activity.getFilesDir();

        // Get the list of files in the internal storage directory
        File[] files = internalStorageDir.listFiles();

        if (files != null) {
            for (File file : files) {
                // Check if the file is an image (you can customize this check based on your file naming convention)
                if (file.isFile() && file.getName().toLowerCase().endsWith(".jpg")) {
                    copyFileToPublicDirectory(file);
                }
            }

            // Notify the user that images have been downloaded
            Toast.makeText(activity, "Images downloaded to public directory", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyFileToPublicDirectory(File sourceFile) {
        try {
            // Open an InputStream for the source file
            InputStream inputStream = new FileInputStream(sourceFile);

            // Create a destination directory in the public directory
            File publicDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "HouseData");

            // Create the directory if it doesn't exist
            if (!publicDirectory.exists()) {
                publicDirectory.mkdirs();
            }

            // Create a destination file within the directory
            File destinationFile = new File(publicDirectory, sourceFile.getName());

            // Open an OutputStream for the destination file
            OutputStream outputStream = new FileOutputStream(destinationFile);

            // Copy the data from InputStream to OutputStream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close the streams
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
