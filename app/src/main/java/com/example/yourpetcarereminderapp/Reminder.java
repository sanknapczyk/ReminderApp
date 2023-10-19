package com.example.yourpetcarereminderapp;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yourpetcarereminderapp.databinding.ActivityReminderBinding;

import java.util.Locale;

/**
 * This activity allows the user to set a reminder for their pet care tasks.
 * The user can select a duration, frequency, and time for the reminder.
 * The selected reminder information is saved to SharedPreferences.
 */
public class Reminder extends AppCompatActivity {

    // Key used for saving and retrieving screen text from SharedPreferences
    static final String SCREEN_TEXT_KEY = "screen_text_key_extra";

    // Arrays for duration and frequency options
    private String[] durations = new String[]{"daily", "weekly", "monthly", "every 2 days", "every 3 days", "every 3 months"};
    private String[] frequencies = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"};

    // Binding object for the activity_reminder layout
    private ActivityReminderBinding binding;

    // SharedPreferences object for storing screen text
    private SharedPreferences sharedPreferences;

    // Type of reminder for the pet
    private String reminderType;

    // Variables for storing selected time
    private int hour, minute;
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReminderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String text = getIntent().getStringExtra(SCREEN_TEXT_KEY);
        reminderType = getIntent().getStringExtra("reminder_type");
        sharedPreferences = getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE);

        ArrayAdapter<String> adapterDurations = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, durations);
        ArrayAdapter<String> adapterFrequencies = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, frequencies);

        // Set the adapters for duration and frequency spinners
        binding.durationSpinner1.setAdapter(adapterDurations);
        binding.frequencySpinner1.setAdapter(adapterFrequencies);

        // Set the reminder text
        binding.reminderText.setText(text);

        // Set click listener for selecting time
        binding.selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popTimePicker();
            }
        });

        // Set click listener for saving the reminder
        binding.saveRmdBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveReminder();
            }
        });
    }

    /**
     * Saves the selected reminder information to SharedPreferences and returns the data to the calling activity.
     */
    private void saveReminder() {
        String selectedDuration = binding.durationSpinner1.getSelectedItem().toString();
        String selectedFrequency = binding.frequencySpinner1.getSelectedItem().toString();
        binding.selectTime.setText(selectedTime);

        // Check if the selectedTime is empty
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            popTimePicker();
            return; // Exit the method without saving the reminder
        }

        try {
            // Save the selected values to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(reminderType + "_reminder_duration", selectedDuration);
            editor.putString(reminderType + "_reminder_frequency", selectedFrequency);
            editor.putString(reminderType + "_reminder_time", selectedTime);
            editor.apply();

            // Retrieve the reminder information string
            String reminderInfo = getReminderInfo(this, reminderType, binding.reminderText.getText().toString());

            // Pass the reminder information back to the PetProfile activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("reminderInfo", reminderInfo);
            setResult(Activity.RESULT_OK, resultIntent);

            // Show a success message
            Toast.makeText(this, "Reminder saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Show an error message
            Toast.makeText(this, "Error saving reminder", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // Finish the activity
        finish();
    }

    /**
     * Retrieves the reminder information from SharedPreferences and builds a string with the
     * reminder details.
     *
     * @param context      The context of the calling activity.
     * @param reminderType The type of the reminder.
     * @param reminderText The text of the reminder.
     * @return The reminder information string.
     */
    public String getReminderInfo(Context context, String reminderType, String reminderText) {
        // Retrieve the reminder values from SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE);
        String selectedDuration = preferences.getString(reminderType + "_reminder_duration", null);
        String selectedFrequency = preferences.getString(reminderType + "_reminder_frequency", null);
        String selectedTime = preferences.getString(reminderType + "_reminder_time", null);

        // Build the reminder information string
        StringBuilder reminderInfoBuilder = new StringBuilder();

        // Append the reminder information if available
        if (selectedDuration != null && selectedFrequency != null && selectedTime != null) {
            reminderInfoBuilder.append(reminderText).append(" Reminder:\n");
            reminderInfoBuilder.append("Duration: ").append(selectedDuration).append("\n");
            reminderInfoBuilder.append("Frequency: ").append(selectedFrequency).append("\n");
            reminderInfoBuilder.append("Time: ").append(selectedTime).append("\n\n");
        }

        return reminderInfoBuilder.toString();
    }

    /**
     * Displays a time picker dialog to select the reminder time.
     */
    private void popTimePicker() {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                binding.selectTime.setText(selectedTime);
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.setTitle("Select time");
        timePickerDialog.show();
    }
}
