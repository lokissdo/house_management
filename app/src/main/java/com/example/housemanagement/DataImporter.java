package com.example.housemanagement;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DataImporter {
    private static final int FILE_PICKER_REQUEST_CODE = 1234;
    private Context context;
    ActivityResultLauncher<Intent> startActivityIntent;
    DatabaseHelper databaseHelper;

    public DataImporter(Context context,DatabaseHelper databaseHelper) {
        this.context = context;
        this.databaseHelper=databaseHelper;
        startActivityIntent =((AppCompatActivity)context).registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                // -1 is OK
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // The activity was successful
                    Log.d("a","here");
                    Intent data = result.getData();

                    //int position = Integer.parseInt(data.getStringExtra("position"));
                    Uri uri = data.getData();
                    saveFileToInternalStorage(uri);

                }
            }
        });
    }

    public void pickCsvFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityIntent.launch(Intent.createChooser(intent, "Open CSV"));
    }


    private void saveFileToInternalStorage(Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r", null);

            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            FileInputStream inputStream = new FileInputStream(fileDescriptor);

            File internalFile = new File(context.getFilesDir(), "data.csv");
            FileOutputStream outputStream = new FileOutputStream(internalFile);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtils.copy(inputStream, outputStream);
            }

            parcelFileDescriptor.close();
            inputStream.close();
            outputStream.close();

            Toast.makeText(context, "Data imported successfully", Toast.LENGTH_SHORT).show();
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            new BatchInsertTask(context, db).execute();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error importing data", Toast.LENGTH_SHORT).show();
        }
    }

    public void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Import");
        builder.setMessage("Are you sure you want to import data?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // User clicked Yes, proceed with import
            pickCsvFile();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // User clicked No, do nothing
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
