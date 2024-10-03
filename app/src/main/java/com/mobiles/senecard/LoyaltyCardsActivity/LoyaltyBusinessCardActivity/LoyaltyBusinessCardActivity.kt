package com.mobiles.senecard.LoyaltyCardsActivity.LoyaltyBusinessCardActivity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.mobiles.senecard.R

class LoyaltyBusinessCardActivity : AppCompatActivity() {

    private val viewModel: LoyaltyBusinessCardViewModel by viewModels()

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_business_card)

        // Configura el botón de regresar
        val backButton: ImageButton = findViewById(R.id.back_button)

        backButton.setOnClickListener {
            viewModel.onBackButtonClicked()
        }

        // Observa el LiveData del ViewModel
        viewModel.backButtonClicked.observe(this, Observer { clicked ->
            if (clicked) {
                // Termina esta actividad y regresa a la anterior
                finish()
            }
        })
    }
}
