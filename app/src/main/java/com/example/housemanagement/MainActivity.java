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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import android.widget.Spinner;
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
    ImageCaptureHelper imageCaptureHelper;
    private Button importDataButton;
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
        importDataButton = findViewById(R.id.import_data);
        DataImporter dataImporter = new DataImporter(MainActivity.this, databaseHelper);
        importDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dataImporter.showConfirmationDialog();
            }
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
















        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        // Create a list of filter options
        List<String> filterOptions = new ArrayList<>();
        filterOptions.add("All Houses");
        filterOptions.add("Houses without Phone Number");
        filterOptions.add("Houses without Address");
        filterOptions.add("Houses without Images");
        filterOptions.add("Houses with Full Information");

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerFilter.setAdapter(adapter);

        // Set a listener to handle item selections
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle item selection and filter houses accordingly
                filterHouses(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

        imageCaptureHelper = new ImageCaptureHelper(this);

        // Set click listener for the "Download Images" button
        Button downloadImagesButton = findViewById(R.id.download_images);
        downloadImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ask for confirmation before downloading images
                imageCaptureHelper.downloadImagesToPublicDirectoryWithConfirmation();
            }
        });
    }
    private void filterHouses(int filterOption) {
        List<HouseModel> filteredList = new ArrayList<>();

        switch (filterOption) {
            case 0:
                // All Houses (no filter)
                filteredList = databaseHelper.getHousesList();
                break;
            case 1:
                // Houses without Phone Number
                filteredList = databaseHelper.filterHousesWithoutPhoneNumber();
                break;
            case 2:
                // Houses without Address
                filteredList = databaseHelper.filterHousesWithoutAddress();
                break;
            case 3:
                // Houses without Images
                filteredList = databaseHelper.filterHousesWithoutImages();
                break;
            case 4:
                // Houses without Images
                filteredList = databaseHelper.filterHousesWithoutFull();
                break;
        }

        adapter.setHouseList(filteredList);
        adapter.notifyDataSetChanged();
    }

}
