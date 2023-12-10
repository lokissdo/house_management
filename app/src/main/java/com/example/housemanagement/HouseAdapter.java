package com.example.housemanagement;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.ViewHolder> {
    private List<HouseModel> houseList;
    private Context context;
    private double longitude;
    private double latitude;
    ImageCaptureHelper imageCaptureHelper;
    DatabaseHelper databaseHelper;

    public static final int PERMISSION_REQUEST_CODE = 101;

    public HouseAdapter(List<HouseModel> houseList, Context context, DatabaseHelper databaseHelper) {
        this.houseList = houseList;
        this.context = context;
        imageCaptureHelper = new ImageCaptureHelper((AppCompatActivity) context);
        this.databaseHelper = databaseHelper;
    }

    public void setHouseList(List<HouseModel> houseList) {
        this.houseList = houseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_house, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HouseModel house = houseList.get(position);

        // Set data to views in the ViewHolder
        holder.ownerNameTextView.setText(house.name);
        if (house.phoneNumber != null)
            holder.phoneNumberTextView.setText(house.phoneNumber);
        else holder.phoneNumberTextView.setText("Chưa có sđt");
        if (house.imagePath != null) {

            holder.imageView.setImageURI(Uri.parse(house.imagePath));

        } else holder.imageView.setImageResource(R.drawable.not_found);

        holder.idTextView.setText(house.id);

        holder.fullImgView.setOnClickListener(view -> {
            // Handle click on full image
            if (house.imagePath != null)
                showZoomDialog(context, house.imagePath);
        });
        holder.editInfoView.setOnClickListener(view -> showEditDialog(house, position, holder));
        if (house.longitude != 0 && house.latitude != 0)
            holder.mapView.setText("Xem địa chỉ");
        else holder.mapView.setText("Chưa có địa chỉ");

        holder.mapView.setOnClickListener(view -> {
            String uri = "http://maps.google.com/?" + house.longitude + "," + house.latitude;
            Uri locationUri = Uri.parse(uri);

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, locationUri);

            context.startActivity(mapIntent);
        });
    }

    private void showZoomDialog(Context context, String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_zoom_image, null);
        builder.setView(dialogView);

        ImageView zoomImageView = dialogView.findViewById(R.id.ih_img_large);
        zoomImageView.setImageURI(Uri.parse(imagePath));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEditDialog(HouseModel house, int position, @NonNull ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_house, null);
        builder.setView(dialogView);

        EditText phoneNumberEditText = dialogView.findViewById(R.id.edit_phone_number);
        phoneNumberEditText.setText(house.phoneNumber);
        ImageView capturePictureIcon = dialogView.findViewById(R.id.icon_capture_picture);
        ImageView locationIcon = dialogView.findViewById(R.id.icon_location);
        TextView locationInfo = dialogView.findViewById(R.id.edit_location_info);
        locationInfo.setText("(" + house.longitude + " ,  " + house.latitude + ")");
        builder.setTitle("Edit House Information")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Update the phone number
                    String newPhoneNumber = phoneNumberEditText.getText().toString().trim();
                    if (!newPhoneNumber.equals("") && newPhoneNumber.length() > 3)
                        house.phoneNumber = newPhoneNumber;

                    house.latitude = latitude;
                    house.longitude = longitude;

                    if (holder.imageView.getTag() != null)
                        house.imagePath = String.valueOf(holder.imageView.getTag());
                    // Capture or choose image and update imagePath

                    databaseHelper.updateHouse(house);

                    // Notify the adapter that the data has changed
                    notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        capturePictureIcon.setOnClickListener(view -> {
            // Implement logic to capture a picture, you can use ImageCaptureHelper

            imageCaptureHelper.showImageOptions(holder.imageView, house.id);
        });
        locationIcon.setOnClickListener(view -> {
            getLatLngFromLocationClick(house, locationInfo);

        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void getLatLngFromLocationClick(HouseModel house, TextView v) {
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }

        // Get the current location using FusedLocationProviderClient
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(context);
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                v.setText("(" + longitude + " ,  " + latitude + " )");
                Log.d(latitude + " ", longitude + " ");
                this.latitude = latitude;
                this.longitude = longitude;
                // Update UI with retrieved latitude and longitude
            } else {
                // Show an error message if location cannot be retrieved
            }
        }).addOnFailureListener(e -> {
            // Handle failure to get location
        });
    }

    @Override
    public int getItemCount() {
        return houseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ownerNameTextView;
        TextView idTextView;
        TextView phoneNumberTextView;
        TextView mapView;
        TextView fullImgView;
        TextView editInfoView;
        ImageView imageView;
        // Add other views as needed

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ownerNameTextView = itemView.findViewById(R.id.ih_owner_name);
            phoneNumberTextView = itemView.findViewById(R.id.ih_phone_number);
            idTextView = itemView.findViewById(R.id.ih_id);
            mapView = itemView.findViewById(R.id.ih_map);
            imageView = itemView.findViewById(R.id.ih_img);
            fullImgView = itemView.findViewById(R.id.ih_full_image);
            editInfoView = itemView.findViewById(R.id.ih_edit_info);
            // Initialize other views as needed
        }
    }
}
