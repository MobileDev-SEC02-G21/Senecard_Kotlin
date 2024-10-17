package com.mobiles.senecard.LoyaltyCardsActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mobiles.senecard.R
import com.mobiles.senecard.LoyaltyCardsActivity.LoyaltyBusinessCardActivity.LoyaltyBusinessCardActivity

class ActivityLoyaltyCards : AppCompatActivity() {

    private val TAG = "ActivityLoyaltyCards"

    // Crear una instancia del ViewModel
    private val viewModel: LoyaltyCardsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_cards)

        Log.d(TAG, "ActivityLoyaltyCards onCreate called")

        val loyaltyCardsContainer = findViewById<ConstraintLayout>(R.id.loyalty_cards_container)

        // Simulación de IDs de prueba
        val businessOwnerId = "UqD7b4Twit3rD98w6Inq"
        val uniandesMemberId = "Z1aNZn8BnA9dxVdT9QaK"
        val storeId = "olNh6XZeAVdRxgEHawJV"

        // Cuando se haga clic en el contenedor, intenta crear o actualizar la RoyaltyCard
        loyaltyCardsContainer.setOnClickListener {
            Log.d(TAG, "Loyalty cards container clicked")

            // Llamar a la función en el ViewModel para simular la creación o actualización de la tarjeta
            viewModel.simulateRoyaltyCardCreation(businessOwnerId, uniandesMemberId, storeId, maxPoints = 8)

            // Iniciar la actividad LoyaltyBusinessCardActivity
            val intent = Intent(this, LoyaltyBusinessCardActivity::class.java)
            startActivity(intent)
        }

        // Configurar el botón de opciones
        val optionsButton = findViewById<ImageButton>(R.id.options_image_view2)
        optionsButton.setOnClickListener {
            onBackPressed() // Esto cerrará la actividad actual
        }
    }
}

