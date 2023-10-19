package com.example.yourpetcarereminderapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yourpetcarereminderapp.databinding.ActivityMainBinding;

/**
 * The MainActivity class serves as the entry point for the app and displays the main screen.
 * It allows users to add a pet and view the pet's profile.
 */
public class MainActivity extends AppCompatActivity {

    // Request code used when navigating to the AddPetScreen activity
    private static final int ADD_PET_REQUEST_CODE = 1;

    // Binding object for the main activity layout
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set click listener for "Add Pet" button
        binding.addPetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent to navigate to AddPetScreen activity
                Intent intent = new Intent(MainActivity.this, AddPetScreen.class);
                startActivityForResult(intent, ADD_PET_REQUEST_CODE);
            }
        });

        // Set click listener for "Pet Profile" button
        binding.petProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Check if pet is saved
                    boolean petSaved = checkPetSaved();
                    if (petSaved) {
                        // Pet is saved, navigate to PetProfile activity
                        Intent intent = new Intent(MainActivity.this, PetProfile.class);
                        startActivity(intent);
                    } else {
                        // Pet is not saved, show a toast message
                        Toast.makeText(MainActivity.this, "No pet saved", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    // Show an error message and print stack trace
                    Toast.makeText(MainActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Checks if a pet is saved in SharedPreferences.
     *
     * @return true if a pet is saved, false otherwise.
     */
    private boolean checkPetSaved() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String petName = preferences.getString("pet_name", null);
        String photoUriString = preferences.getString("image_uri", null);
        return petName != null && photoUriString != null;
    }
}
