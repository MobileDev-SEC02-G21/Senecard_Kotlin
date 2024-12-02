package com.mobiles.senecard.activitySettings
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivitySettingsBinding

class ActivitySettings : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModelSettings: ViewModelSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("SettingsPreferences", MODE_PRIVATE)
        viewModelSettings = ViewModelProvider(
            this,
            ViewModelSettingsFactory(sharedPreferences)
        )[ViewModelSettings::class.java]

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.backImageView.setOnClickListener {
            viewModelSettings.backImageViewClicked()
        }

        binding.englishButton.setOnClickListener {
            viewModelSettings.updateLanguage("en")
        }

        binding.spanishButton.setOnClickListener {
            viewModelSettings.updateLanguage("es")
        }

        binding.lightButton.setOnClickListener {
            viewModelSettings.updateTheme("Light")
        }

        binding.darkButton.setOnClickListener {
            viewModelSettings.updateTheme("Dark")
        }

        binding.pushButton.setOnClickListener {
            viewModelSettings.togglePushNotifications()
        }

        binding.rememberButton.setOnClickListener {
            viewModelSettings.toggleRememberNotifications()
        }

        binding.advertisementsButton.setOnClickListener {
            viewModelSettings.toggleAdvertisementsNotifications()
        }
    }

    private fun setObservers() {
        viewModelSettings.language.observe(this) { language ->
            if (language == "en") {
                binding.englishButton.isEnabled = false
                binding.spanishButton.isEnabled = true
                binding.englishButton.setBackgroundResource(R.drawable.home_categories_scroll_button_selected_background)
                binding.spanishButton.setBackgroundResource(R.drawable.home_categories_scroll_button_background)
                binding.englishButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else if (language == "es"){
                binding.spanishButton.isEnabled = false
                binding.englishButton.isEnabled = true
                binding.spanishButton.setBackgroundResource(R.drawable.home_categories_scroll_button_selected_background)
                binding.englishButton.setBackgroundResource(R.drawable.home_categories_scroll_button_background)
                binding.spanishButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }

        viewModelSettings.theme.observe(this) { theme ->
            if (theme == "Light") {
                binding.lightButton.isEnabled = false
                binding.darkButton.isEnabled = true
                binding.lightButton.setBackgroundResource(R.drawable.home_categories_scroll_button_selected_background)
                binding.darkButton.setBackgroundResource(R.drawable.home_categories_scroll_button_background)
                binding.lightButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else if (theme == "Dark") {
                binding.darkButton.isEnabled = false
                binding.lightButton.isEnabled = true
                binding.darkButton.setBackgroundResource(R.drawable.home_categories_scroll_button_selected_background)
                binding.lightButton.setBackgroundResource(R.drawable.home_categories_scroll_button_background)
                binding.darkButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }

        viewModelSettings.pushNotifications.observe(this) { isEnabled ->
            if (isEnabled) {
                binding.pushButton.text = "On"
                binding.pushButton.setBackgroundResource(R.drawable.home_categories_scroll_button_selected_background)
                binding.pushButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                binding.pushButton.text = "Off"
                binding.pushButton.setBackgroundResource(R.drawable.home_categories_scroll_button_background)
            }
        }

        viewModelSettings.rememberNotifications.observe(this) { isEnabled ->
            if (isEnabled) {
                binding.rememberButton.text = "On"
                binding.rememberButton.setBackgroundResource(R.drawable.home_categories_scroll_button_selected_background)
                binding.rememberButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                binding.rememberButton.text = "Off"
                binding.rememberButton.setBackgroundResource(R.drawable.home_categories_scroll_button_background)
            }
        }

        viewModelSettings.advertisementsNotifications.observe(this) { isEnabled ->
            if (isEnabled) {
                binding.advertisementsButton.text = "On"
                binding.advertisementsButton.setBackgroundResource(R.drawable.home_categories_scroll_button_selected_background)
                binding.advertisementsButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                binding.advertisementsButton.text = "Off"
                binding.advertisementsButton.setBackgroundResource(R.drawable.home_categories_scroll_button_background)
            }
        }
        viewModelSettings.navigateToActivityBack.observe(this) { navigate ->
            if (navigate) {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }
}