package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerEditProfile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerProfile.ActivityBusinessOwnerProfile
import com.mobiles.senecard.databinding.ActivityBusinessOwnerEditProfileBinding
import com.mobiles.senecard.model.entities.Store
import com.mobiles.senecard.model.entities.User

class ActivityBusinessOwnerEditProfile : AppCompatActivity() {

    private lateinit var binding: ActivityBusinessOwnerEditProfileBinding
    private val viewModel: ViewModelBusinessOwnerEditProfile by viewModels()

    private lateinit var getImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupImagePicker()
        setupObservers()
        setupListeners()
        viewModel.fetchProfileData()
    }

    private fun setupBinding() {
        binding = ActivityBusinessOwnerEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupImagePicker() {
        getImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    val data = result.data
                    data?.data?.let { uri ->
                        selectedImageUri = uri
                        binding.storeImageView.setImageURI(uri)
                    }
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(result.data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            user?.let { populateUserData(it) }
        }

        viewModel.store.observe(this) { store ->
            store?.let { populateStoreData(it) }
        }

        viewModel.saveStatus.observe(this) { isSuccessful ->
            if (isSuccessful) {
                Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                navigateTo(NavigationDestination.PROFILE)
            } else {
                Toast.makeText(this, "Failed to save profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            if (validateFields()) {
                saveProfileData()
            }
        }

        binding.editStoreImageButton.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent { intent -> getImageLauncher.launch(intent) }
        }

        binding.backButton.setOnClickListener {
            navigateTo(NavigationDestination.PROFILE)
        }
    }

    private fun populateUserData(user: User) {
        binding.apply {
            userNameEditText.setText(user.name)
            userEmailEditText.setText(user.email)
            userPhoneEditText.setText(user.phone)
        }
    }

    private fun populateStoreData(store: Store) {
        binding.apply {
            businessNameEditText.setText(store.name)
            addressEditText.setText(store.address)
            selectedImageUri = null
            Glide.with(this@ActivityBusinessOwnerEditProfile)
                .load(store.image)
                .placeholder(R.drawable.no_image_placeholder)
                .into(storeImageView)
        }
    }

    private fun saveProfileData() {
        val user = User(
            id = viewModel.user.value?.id,
            name = binding.userNameEditText.text.toString(),
            email = binding.userEmailEditText.text.toString(),
            phone = binding.userPhoneEditText.text.toString(),
            role = viewModel.user.value?.role
        )

        val store = Store(
            id = viewModel.store.value?.id,
            name = binding.businessNameEditText.text.toString(),
            address = binding.addressEditText.text.toString(),
            image = null
        )

        viewModel.saveProfileData(user, store, selectedImageUri)
    }

    private fun navigateTo(destination: NavigationDestination) {
        when (destination) {
            NavigationDestination.PROFILE -> {
                val intent = Intent(this, ActivityBusinessOwnerProfile::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun validateFields(): Boolean {
        val nameRegex = Regex("^[a-zA-Z ]+$")
        val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        val phoneRegex = Regex("^\\d{10,15}$") // Allow phone numbers with 10-15 digits

        val userName = binding.userNameEditText.text.toString().trim()
        val userEmail = binding.userEmailEditText.text.toString().trim()
        val userPhone = binding.userPhoneEditText.text.toString().trim()
        val businessName = binding.businessNameEditText.text.toString().trim()
        val businessAddress = binding.addressEditText.text.toString().trim()

        if (userName.isEmpty() || !nameRegex.matches(userName)) {
            Toast.makeText(this, "Please enter a valid name (letters only).", Toast.LENGTH_SHORT).show()
            return false
        }

        if (userEmail.isEmpty() || !emailRegex.matches(userEmail)) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (userPhone.isEmpty() || !phoneRegex.matches(userPhone)) {
            Toast.makeText(this, "Please enter a valid phone number (10-15 digits).", Toast.LENGTH_SHORT).show()
            return false
        }

        if (businessName.isEmpty()) {
            Toast.makeText(this, "Business name cannot be empty.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (businessAddress.isEmpty()) {
            Toast.makeText(this, "Business address cannot be empty.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 1001
    }
}
