package com.example.housemanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.housemanagement.HouseModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "houses";
    private static final int DATABASE_VERSION = 1;
    private  Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context =context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the "houses" table if it doesn't exist
        if(! isTableExists("houses",db)){
            db.execSQL("CREATE TABLE IF NOT EXISTS houses " +
                    "(id TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "image_path TEXT DEFAULT NULL, " +
                    "latitude REAL DEFAULT NULL, " +
                    "longitude REAL DEFAULT NULL, " +
                    "phone_number TEXT DEFAULT NULL)");
//            new BatchInsertTask(context, db).execute();

        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database schema upgrades if needed
    }

    public Cursor getAllHouses() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT id, name, phone_number, latitude, longitude, image_path FROM houses", null);
    }
    private boolean isTableExists(String tableName, SQLiteDatabase database) {
        Cursor cursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName}
        );
        boolean tableExists = cursor.moveToFirst();
        cursor.close();
        return tableExists;
    }
    public void exportDataToCsv() {
        List<HouseModel> houseList = getHousesList();

        // Check if external storage is available
        if (isExternalStorageWritable()) {
//            File exportDir = new File(Environment.getExternalStorageDirectory(), "HouseData");
            File exportDir =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"HouseData");

            if (!exportDir.exists() && !exportDir.mkdirs()) {
                Log.e("DatabaseHelper", "Failed to create directory: " + exportDir.getAbsolutePath());
                return;
            }
            File file = new File(exportDir, "houses.csv");
            try {
                FileWriter fw = new FileWriter(file);

                // Write header to CSV file
                fw.append("ID,Name,Phone Number,Latitude,Longitude,Image Path\n");



                // Write data to CSV file
                for (HouseModel house : houseList) {
                    String latitudeString = String.valueOf(house.latitude);
                    String longitudeString = String.valueOf(house.longitude);
                    Log.d("la",latitudeString);
                    Log.d("longitudeString",longitudeString);
                    fw.append(String.format(Locale.getDefault(),
                            "%s,%s,%s,%s,%s,%s\n",
                            house.id,
                            house.name,
                            house.phoneNumber,
                            latitudeString,
                            longitudeString,
                            house.imagePath));
                }

                // Close the FileWriter
                fw.close();

                Log.d("DatabaseHelper", "Data exported to CSV successfully: " + file.getAbsolutePath());
                Toast.makeText(context,"Data exported to CSV successfully: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("DatabaseHelper", "Error exporting data to CSV: " + e.getMessage());
            }
        } else {
            Log.e("DatabaseHelper", "External storage not available or writable.");
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    public List<HouseModel> getHousesList() {
        List<HouseModel> houseList = new ArrayList<>();
        Cursor cursor = getAllHouses();

        if (cursor.moveToFirst()) {
            do {
                int idColumnIndex = cursor.getColumnIndex("id");
                int nameColumnIndex = cursor.getColumnIndex("name");
                int phoneNumberColumnIndex = cursor.getColumnIndex("phone_number");
                int latitudeColumnIndex = cursor.getColumnIndex("latitude");
                int longitudeColumnIndex = cursor.getColumnIndex("longitude");
                int image_pathColumnIndex = cursor.getColumnIndex("image_path");

                // Handle the case where latitude or longitude columns may not exist

                HouseModel house = new HouseModel();
                house.id = cursor.getString(idColumnIndex);
                house.name = cursor.getString(nameColumnIndex);
                house.phoneNumber = cursor.getString(phoneNumberColumnIndex);
                house.latitude = cursor.getDouble(latitudeColumnIndex);
                house.longitude = cursor.getDouble(longitudeColumnIndex);
                house.imagePath = cursor.getString(image_pathColumnIndex);

                houseList.add(house);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return houseList;
    }
    public void updateHouse(HouseModel updatedHouse) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", updatedHouse.name);
        values.put("phone_number", updatedHouse.phoneNumber);
        values.put("latitude", updatedHouse.latitude);
        values.put("longitude", updatedHouse.longitude);
        values.put("image_path", updatedHouse.imagePath);

        // Update the record based on the house ID
        db.update("houses", values, "id=?", new String[]{updatedHouse.id});

        // Close the database
        db.close();
    }

    public List<HouseModel> filterHousesBasedOnText(String searchText) {
        List<HouseModel> filteredList = new ArrayList<>();
        List<HouseModel> houseList = getHousesList();

        for (HouseModel house : houseList) {
            // Implement your own logic for filtering based on your requirements
            if (house.name.toLowerCase(Locale.getDefault()).contains(searchText.toLowerCase(Locale.getDefault()))
                    || house.id.toLowerCase(Locale.getDefault()).contains(searchText.toLowerCase(Locale.getDefault()))) {
                filteredList.add(house);
            }
        }

        return filteredList;
    }

    public List<HouseModel> filterHousesWithoutPhoneNumber() {
        List<HouseModel> filteredList = new ArrayList<>();
        List<HouseModel> houseList = getHousesList();

        for (HouseModel house : houseList) {
            // Implement your own logic for filtering based on your requirements
            if (house.phoneNumber == null)
                filteredList.add(house);
        }

        return filteredList;
    }

    public List<HouseModel> filterHousesWithoutAddress() {
        List<HouseModel> filteredList = new ArrayList<>();
        List<HouseModel> houseList = getHousesList();

        for (HouseModel house : houseList) {
            // Implement your own logic for filtering based on your requirements
            if (house.latitude == 0 && house.longitude == 0)
                filteredList.add(house);
        }

        return filteredList;
    }

    public List<HouseModel> filterHousesWithoutImages() {
        List<HouseModel> filteredList = new ArrayList<>();
        List<HouseModel> houseList = getHousesList();

        for (HouseModel house : houseList) {
            // Implement your own logic for filtering based on your requirements
            if (house.imagePath == null)
                filteredList.add(house);
        }

        return filteredList;
    }

    public List<HouseModel> filterHousesWithoutFull() {
        List<HouseModel> filteredList = new ArrayList<>();
        List<HouseModel> houseList = getHousesList();

        for (HouseModel house : houseList) {
            // Implement your own logic for filtering based on your requirements
            if (house.imagePath != null && house.longitude !=0 && house.latitude != 0 && house.phoneNumber != null)
                filteredList.add(house);
        }

        return filteredList;
    }
}
