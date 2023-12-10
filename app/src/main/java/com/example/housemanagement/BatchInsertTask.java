package com.example.housemanagement;
import android.content.ContentValues;
import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// ...

public class BatchInsertTask extends AsyncTask<Void, Void, Void> {
    private final Context context;
    private  SQLiteDatabase db;

    public BatchInsertTask(Context context, SQLiteDatabase db) {
        this.context = context;
        this.db = db;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // Read CSV file from assets
            InputStream inputStream = context.getAssets().open("data.csv");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // Skip the header line
            bufferedReader.readLine();
            Log.d("insert","procesisng");
            // Process each line in the CSV and insert into the database
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d("line",line );
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String id = parts[0];
                    String name = parts[1];

                    // Insert data into the database
                    ContentValues values = new ContentValues();
                    values.put("id", id);
                    values.put("name", name);

                    long newRowId = db.insert("houses", null, values);

                    // Handle the result as needed, e.g., log the new row ID or show a toast
                    if (newRowId != -1) {
                        // Successful insertion
                        // Log or show a success message
                    } else {
                        // Failed insertion
                        // Log or show an error message
                    }
                }
                Log.d("insert",String.valueOf(parts.length) );


            }

            // Close the streams
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, "Batch insertion completed", Toast.LENGTH_SHORT).show();
    }
}