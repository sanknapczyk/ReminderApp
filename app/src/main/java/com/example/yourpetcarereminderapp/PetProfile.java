package com.example.yourpetcarereminderapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yourpetcarereminderapp.databinding.ActivityPetProfileBinding;

/**
 * This activity displays the profile of a pet, including its name, photo, and reminder information.
 */
public class PetProfile extends AppCompatActivity {

    // Binding object for the activity_pet_profile layout
    private ActivityPetProfileBinding binding;

    // Reminder object for the pet
    public Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPetProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Create an instance of the Reminder class
        reminder = new Reminder();

        // Set click on listener for editing profile
        binding.editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PetProfile.this, AddPetScreen.class);
                startActivity(intent);
            }
        });

        // Retrieve the saved pet name and photo URI from SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences != null) {
            // Retrieve the pet name and photo URI
            String petName = preferences.getString("pet_name", null);
            String photoUriString = preferences.getString("image_uri", null);

            // Display the pet name
            if (petName != null) {
                binding.petName.setText(petName);
            } else {
                binding.petName.setText(""); // Set an empty string if pet name is not available
            }

            // Display the pet photo
            if (photoUriString != null && !photoUriString.isEmpty()) {
                Uri photoUri = Uri.parse(photoUriString);
                binding.petPhoto.setImageURI(photoUri);
            }

            try {
                // Retrieve the reminder information from the Reminder class for different types
                String foodReminderInfo = reminder.getReminderInfo(this, "food", "Food");
                String waterReminderInfo = reminder.getReminderInfo(this, "water", "Water");
                String cleaningReminderInfo = reminder.getReminderInfo(this, "cleaning", "Cleaning");

                // Concatenate the reminder information into a single string
                StringBuilder reminderInfoBuilder = new StringBuilder();
                reminderInfoBuilder.append(foodReminderInfo);
                reminderInfoBuilder.append(waterReminderInfo);
                reminderInfoBuilder.append(cleaningReminderInfo);

                // Check if the reminder information is passed from the Reminder activity
                if (getIntent().hasExtra("reminderInfo")) {
                    String additionalReminderInfo = getIntent().getStringExtra("reminderInfo");
                    reminderInfoBuilder.append(additionalReminderInfo);
                }

                String reminderInfo = reminderInfoBuilder.toString();

                // Display the reminder information in the TextView
                binding.reminderInfo.setText(reminderInfo);
            } catch (Exception e) {
                // Exceptions that occur during reminder retrieval
                e.printStackTrace();
                Toast.makeText(this, "Error retrieving reminder information", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Set the on activity result for getting the reminder info from Reminder class
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            String reminderInfo = data.getStringExtra("reminderInfo");
            if (reminderInfo != null) {
                // Display the reminder information in the TextView
                binding.reminderInfo.setText(reminderInfo);
            }
        }
    }
}

