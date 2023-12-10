package com.example.housemanagement;


import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private HouseAdapter adapter;
    private List<HouseModel> houseList;
    private DatabaseHelper databaseHelper;
    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseHelper = new DatabaseHelper(this);
        SearchView searchView = findViewById(R.id.buttonSearch);
        Button export = findViewById(R.id.export_data);
        export.setOnClickListener(v -> {
            databaseHelper.exportDataToCsv();
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search submission
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<HouseModel> filteredList = databaseHelper.filterHousesBasedOnText(newText);
                adapter.setHouseList(filteredList);
                adapter.notifyDataSetChanged();

                return true;
            }
        });

        // imageView.setOnClickListener(v -> showImageOptions());

        RecyclerView recyclerView = findViewById(R.id.recycler_item);
        houseList = databaseHelper.getHousesList(); // Implement this method to get data from the database
        adapter = new HouseAdapter(houseList, MainActivity.this, databaseHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }


}
