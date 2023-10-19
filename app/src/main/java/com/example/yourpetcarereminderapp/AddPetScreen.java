package com.example.yourpetcarereminderapp;

import static com.example.yourpetcarereminderapp.Reminder.SCREEN_TEXT_KEY;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.yourpetcarereminderapp.databinding.ActivityAddPetScreenBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The AddPetScreen class allows users to add a new pet by providing a name and setting reminders.
 * Users can also select a photo for the pet.
 */
public class AddPetScreen extends AppCompatActivity {

    // Request code used for gallery selection
    private static final int GALLERY_REQ_CODE = 1;

    // Request code used for storage permission
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;

    // Binding object for the activity_add_pet_screen layout
    private ActivityAddPetScreenBinding binding;

    // Uri for the selected image
    private Uri selectedImageUri;

    // Type of reminder for the pet
    private String reminderType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPetScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set click listener for "Add Photo" button
        binding.addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkStoragePermission();
            }
        });

        // Set click listener for "Food" button
        binding.foodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReminder("Food Reminder", "food");
            }
        });

        // Set click listener for "Water" button
        binding.waterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReminder("Water Reminder", "water");
            }
        });

        // Set click listener for "Cleaning" button
        binding.cleaningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReminder("Cleaning Reminder", "cleaning");
            }
        });

        // Set click listener for "Save Pet" button
        binding.savePetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePet();
            }
        });
    }

    // Checks storage permission, if API>=29 no permission dialog needed
    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                selectImage();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    // Selects image from the gallery
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQ_CODE);
    }

    // Set on request permission result for storage permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Set on activity result for getting the image and saving the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQ_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                selectedImageUri = data.getData();
                saveImage(selectedImageUri);
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            // Check if the result is from the Reminder activity
            if (data != null && data.hasExtra("duration") && data.hasExtra("frequency") && data.hasExtra("time")) {
                String duration = data.getStringExtra("duration");
                String frequency = data.getStringExtra("frequency");
                String time = data.getStringExtra("time");

                // Save the reminder information to SharedPreferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(reminderType + "_reminder_duration", duration);
                editor.putString(reminderType + "_reminder_frequency", frequency);
                editor.putString(reminderType + "_reminder_time", time);
                editor.apply();

                // Show a success message
                Toast.makeText(this, "Reminder information saved", Toast.LENGTH_SHORT).show();

                // Save the pet information and navigate to PetProfile activity
                savePet();
            }
        }
    }

    /**
     * Saves the selected image to the cache directory.
     *
     * @param imageUri The URI of the selected image.
     */
    private void saveImage(Uri imageUri) {
        File cacheDir = getCacheDir();
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File outputFile = new File(cacheDir, "pet_image.jpg");

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("image_uri", outputFile.getAbsolutePath());
            editor.apply();

            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

     //Saves the pet's name and navigates back to the main screen
    private void savePet() {
        String petName = binding.editTextTPetName.getText().toString();

        if (petName.isEmpty()) {
            Toast.makeText(this, "Please enter a pet name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        saveName(petName);
        saveImage(selectedImageUri);

        Toast.makeText(this, "Name and image saved successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AddPetScreen.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Saves the pet's name to SharedPreferences.
     *
     * @param petName The name of the pet.
     */
    private void saveName(String petName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("pet_name", petName);
        editor.apply();
    }

    /**
     * Opens the Reminder activity with the specified reminder type.
     *
     * @param reminderText The text of the reminder.
     */
    private void openReminder(String reminderText, String reminderInfo) {
        Intent intent = new Intent(AddPetScreen.this, Reminder.class);
        intent.putExtra(SCREEN_TEXT_KEY, reminderText);
        intent.putExtra("reminder_type", reminderInfo);
        startActivity(intent);
    }
}