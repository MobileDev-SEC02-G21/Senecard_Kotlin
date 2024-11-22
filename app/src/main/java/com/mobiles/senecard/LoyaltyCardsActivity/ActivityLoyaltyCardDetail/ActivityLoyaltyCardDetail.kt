package com.mobiles.senecard.LoyaltyCardsActivity.ActivityLoyaltyCardDetail

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mobiles.senecard.R
import com.mobiles.senecard.adapters.LoyaltyCardAdapter
import com.mobiles.senecard.viewmodel.ViewModelLoyaltyCardDetail

class ActivityLoyaltyCardDetail : AppCompatActivity() {

    private val viewModel: ViewModelLoyaltyCardDetail by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_card_detail)

        setupBackButton()
        observeViewModel()

        val storeName = intent.getStringExtra(EXTRA_STORE_NAME) ?: "Tienda Desconocida"
        val storeAddress = intent.getStringExtra(LoyaltyCardAdapter.EXTRA_STORE_ADDRESS) ?: "Dirección Desconocida"
        val storeImage = intent.getStringExtra(EXTRA_STORE_IMAGE)
        val points = intent.getIntExtra(EXTRA_POINTS, 0)
        val maxPoints = intent.getIntExtra(EXTRA_MAX_POINTS, 0)

        Log.d("ActivityLoyaltyCardDetail", "Store Name: $storeName")
        Log.d("ActivityLoyaltyCardDetail", "Current Points: $points")
        Log.d("ActivityLoyaltyCardDetail", "Max Points: $maxPoints")

        findViewById<TextView>(R.id.location_text_view).text = storeAddress
        setupUI(storeName, storeImage, points, maxPoints)
        displayStamps(points, maxPoints)
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            setResult(RESULT_OK) // Indica que se cambió algo
            finish() // Termina la actividad y vuelve a la anterior
        }
    }

    private fun observeViewModel() {
        viewModel.backButtonClicked.observe(this) { clicked ->
            if (clicked) {
                setResult(RESULT_OK) // Establece el resultado
                finish()
            }
        }
    }

    private fun setupUI(storeName: String, storeImage: String?, points: Int, maxPoints: Int) {
        findViewById<TextView>(R.id.harvest_text_view).text = storeName

        val storeImageView = findViewById<ImageView>(R.id.reward_image)
        if (!storeImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(storeImage)
                .placeholder(R.mipmap.icon_image_landscape)
                .into(storeImageView)
        } else {
            storeImageView.setImageResource(R.mipmap.icon_image_landscape)
        }

        val remainingStamps = maxPoints - points
        findViewById<TextView>(R.id.stamps_needed_text_view).text = "Sellos faltantes: $remainingStamps"

        Log.d("ActivityLoyaltyCardDetail", "Remaining Stamps: $remainingStamps")
    }

    private fun displayStamps(points: Int, maxPoints: Int) {
        val stampsContainer1 = findViewById<LinearLayout>(R.id.stamps_container_1)
        val stampsContainer2 = findViewById<LinearLayout>(R.id.stamps_container_2)

        stampsContainer1.removeAllViews()
        stampsContainer2.removeAllViews()

        // Limitar el ciclo hasta el valor mínimo entre `points` y `maxPoints`
        for (i in 1..minOf(points, maxPoints)) {
            val stampImageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setImageResource(R.mipmap.icon_stamp)
            }

            if (i <= 4) {
                stampsContainer1.addView(stampImageView)
            } else {
                stampsContainer2.addView(stampImageView)
            }
        }
    }


    companion object {
        const val EXTRA_STORE_NAME = "STORE_NAME"
        const val EXTRA_STORE_ADDRESS = "STORE_ADDRESS"
        const val EXTRA_STORE_IMAGE = "STORE_IMAGE"
        const val EXTRA_POINTS = "POINTS"
        const val EXTRA_MAX_POINTS = "MAX_POINTS"
    }
}