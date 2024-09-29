package com.mobiles.senecard.activitiesSignUp.activitySignUp

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.activitiesSignIn.activitySignIn.ActivitySignIn
import com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner1.ActivitySignUpStoreOwner1
import com.mobiles.senecard.activitiesSignUp.activitySignUpUniandesMember.ActivitySignUpUniandesMember
import com.mobiles.senecard.databinding.ActivitySignUpBinding

class ActivitySignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModelSignUp: ViewModelSignUp by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.backImageView.setOnClickListener {
            viewModelSignUp.backImageViewClicked()
        }
        binding.uniandesCommunityButton.setOnClickListener {
            viewModelSignUp.uniandesCommunityButtonClicked()
        }
        binding.storeOwnerButton.setOnClickListener {
            viewModelSignUp.storeOwnerButtonClicked()
        }
        binding.signInTextView.setOnClickListener {
            viewModelSignUp.signInTextViewClicked()
        }
    }

    private fun setObservers() {
        viewModelSignUp.navigateToActivityInitial.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivityInitial::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                startActivity(intent, options.toBundle())
                viewModelSignUp.onNavigated()
            }
        }
        viewModelSignUp.navigateToActivitySignUpUniandesMember.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignUpUniandesMember::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelSignUp.onNavigated()
            }
        }
        viewModelSignUp.navigateToActivitySignUpStoreOwner1.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignUpStoreOwner1::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelSignUp.onNavigated()
            }
        }
        viewModelSignUp.navigateToActivitySignIn.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivitySignIn::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_up,
                    R.anim.slide_out_down
                )
                startActivity(intent, options.toBundle())
                viewModelSignUp.onNavigated()
            }
        }
    }
}