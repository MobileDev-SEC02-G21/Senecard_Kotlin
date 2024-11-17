package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisementsCreate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.databinding.ActivityBusinessOwnerAdvertisementsCreateBinding
import com.mobiles.senecard.model.entities.Advertisement
import java.time.LocalDate
import java.time.format.DateTimeParseException

class ActivityBusinessOwnerAdvertisementsCreate : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerAdvertisementsCreateBinding
    private val viewModel: ViewModelBusinessOwnerAdvertisementsCreate by viewModels()
    // Variables to hold the received values
    private var businessOwnerId: String? = null
    private var storeId: String? = null
    private var storeName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding
        binding = ActivityBusinessOwnerAdvertisementsCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the extras from the intent
        businessOwnerId = intent.getStringExtra("businessOwnerId")
        storeId = intent.getStringExtra("storeId")
        storeName = intent.getStringExtra("storeName")

        // Set up click listeners
        binding.btnSave.setOnClickListener {
            saveAdvertisement()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun saveAdvertisement() {
        val title = binding.advertisementTitleInput.text.toString().trim()
        val description = binding.advertisementDescriptionInput.text.toString().trim()
        val startDay = binding.advertisementStartDayInput.text.toString().trim()
        val startMonth = binding.advertisementStartMonthInput.text.toString().trim()
        val startYear = binding.advertisementStartYearInput.text.toString().trim()
        val endDay = binding.advertisementEndDayInput.text.toString().trim()
        val endMonth = binding.advertisementEndMonthInput.text.toString().trim()
        val endYear = binding.advertisementEndYearInput.text.toString().trim()

        // Basic validation for empty fields
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (startDay.isEmpty() || startMonth.isEmpty() || startYear.isEmpty() || endDay.isEmpty() || endMonth.isEmpty() || endYear.isEmpty()) {
            Toast.makeText(this, "Please fill in all date fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validation for numeric-only input for dates
        val dayRegex = Regex("^\\d{1,2}\$")
        val monthRegex = Regex("^\\d{1,2}\$")
        val yearRegex = Regex("^\\d{4}\$")

        if (!startDay.matches(dayRegex) || !startMonth.matches(monthRegex) || !startYear.matches(yearRegex) ||
            !endDay.matches(dayRegex) || !endMonth.matches(monthRegex) || !endYear.matches(yearRegex)) {
            Toast.makeText(this, "Please enter valid numbers for day, month, and year", Toast.LENGTH_SHORT).show()
            return
        }

        // Validation for valid day, month, and year ranges
        val startDayInt = startDay.toInt()
        val startMonthInt = startMonth.toInt()
        val startYearInt = startYear.toInt()
        val endDayInt = endDay.toInt()
        val endMonthInt = endMonth.toInt()
        val endYearInt = endYear.toInt()

        if (startDayInt !in 1..31 || endDayInt !in 1..31 || startMonthInt !in 1..12 || endMonthInt !in 1..12) {
            Toast.makeText(this, "Please enter valid day and month values", Toast.LENGTH_SHORT).show()
            return
        }

        // Combine the date fields into start and end LocalDate objects
        try {
            val startDate = LocalDate.of(startYearInt, startMonthInt, startDayInt)
            val endDate = LocalDate.of(endYearInt, endMonthInt, endDayInt)

            // Validate that start date is before end date
            if (startDate.isAfter(endDate)) {
                Toast.makeText(this, "Start date must be before end date", Toast.LENGTH_SHORT).show()
                return
            }

            // Pass data to ViewModel for saving
            viewModel.saveAdvertisement(title, description, startDate.toString(), endDate.toString(), businessOwnerId!!)

            Toast.makeText(this, "Advertisement saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: DateTimeParseException) {
            Toast.makeText(this, "Please enter a valid date", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
