package com.mobiles.senecard.LoyaltyCardsActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mobiles.senecard.R
import com.mobiles.senecard.LoyaltyCardsActivity.LoyaltyBusinessCardActivity.LoyaltyBusinessCardActivity

class ActivityLoyaltyCards : AppCompatActivity() {

    private val TAG = "ActivityLoyaltyCards"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_cards)

        Log.d(TAG, "ActivityLoyaltyCards onCreate called")

        val loyaltyCardsContainer = findViewById<ConstraintLayout>(R.id.loyalty_cards_container)

        loyaltyCardsContainer.setOnClickListener {
            Log.d(TAG, "Loyalty cards container clicked")
            val intent = Intent(this, LoyaltyBusinessCardActivity::class.java)
            startActivity(intent)
        }

        // Configurar el botón de opciones
        val optionsButton = findViewById<ImageButton>(R.id.options_image_view2)
        optionsButton.setOnClickListener {
            // Lógica para el botón de opciones
            onBackPressed() // Esto cerrará la actividad actual
        }
    }
}
