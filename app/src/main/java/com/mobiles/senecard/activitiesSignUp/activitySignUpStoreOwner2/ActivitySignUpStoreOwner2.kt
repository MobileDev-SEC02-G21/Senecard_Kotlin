package com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.mobiles.senecard.CustomDialog
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner1.ActivitySignUpStoreOwner1
import com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner3.ActivitySignUpStoreOwner3
import com.mobiles.senecard.databinding.ActivitySignUpStoreOwner2Binding

class ActivitySignUpStoreOwner2 : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpStoreOwner2Binding
    private val viewModelSignUpStoreOwner2: ViewModelSignUpStoreOwner2 by viewModels()

    private lateinit var getImageLauncher: ActivityResultLauncher<Intent>
    private var storeImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpStoreOwner2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                getImageLauncher.launch(intent)
            } else {
                Toast.makeText(this, getString(R.string.sign_up_store_owner_2_permission_denied_to_access_images), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setElements() {
        getImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                data?.data?.let { uri ->
                    storeImageUri = uri
                    binding.storeImageImageView.setImageURI(uri)
                }
            }
        }
        binding.backImageView.setOnClickListener {
            viewModelSignUpStoreOwner2.backImageViewClicked()
        }
        binding.storeImageImageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 101)
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                getImageLauncher.launch(intent)
            }
        }
        binding.nextButton.setOnClickListener {
            showMessage("Please wait one moment while processing the information", "loading")
            viewModelSignUpStoreOwner2.nextButtonClicked(
                storeName = binding.storeNameEditText.text.toString(),
                storeAddress = binding.storeAddressEditText.text.toString(),
                storeCategory = binding.storeCategorySpinner.selectedItem.toString(),
                storeImage = storeImageUri
            )
        }
    }

    private fun setObservers() {
        viewModelSignUpStoreOwner2.navigateToActivitySignUpStoreOwner1.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignUpStoreOwner1::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                startActivity(intent, options.toBundle())
                viewModelSignUpStoreOwner2.onNavigated()
            }
        }
        viewModelSignUpStoreOwner2.navigateToActivitySignUpStoreOwner3.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignUpStoreOwner3::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelSignUpStoreOwner2.onNavigated()
            }
        }
        viewModelSignUpStoreOwner2.message.observe(this) { message ->
            when (message) {
                "store_name_empty" -> showMessage(getString(R.string.sign_up_store_owner_2_store_name_empty))
                "store_address_empty" -> showMessage(getString(R.string.sign_up_store_owner_2_store_address_empty))
                "store_category_empty" -> showMessage(getString(R.string.sign_up_store_owner_2_store_category_empty))
                "store_image_empty" -> showMessage(getString(R.string.sign_up_store_owner_2_store_image_empty))
            }
        }
    }

    private fun showMessage(message: String, type: String = "info") {
        CustomDialog.showCustomDialog(supportFragmentManager, message, type)
    }
}