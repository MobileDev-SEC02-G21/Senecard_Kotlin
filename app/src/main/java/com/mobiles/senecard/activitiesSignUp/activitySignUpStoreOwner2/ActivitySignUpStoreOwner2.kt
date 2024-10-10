package com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.github.dhaval2404.imagepicker.ImagePicker
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

    private fun setElements() {
        getImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    val data = result.data
                    data?.data?.let { uri ->
                        storeImageUri = uri
                        binding.storeImageImageView.setImageURI(uri)
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
        binding.backImageView.setOnClickListener {
            viewModelSignUpStoreOwner2.backImageViewClicked()
        }
        binding.storeImageImageView.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent { intent ->
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
                CustomDialog.hideCustomDialog()
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