package com.mobiles.senecard.LoyaltyCardsActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobiles.senecard.LoyaltyCardsActivity.ActivityLoyaltyCardDetail.ActivityLoyaltyCardDetail
import com.mobiles.senecard.R
import com.mobiles.senecard.adapters.LoyaltyCardAdapter
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.LoyaltyCard
import com.mobiles.senecard.model.entities.Store
import kotlinx.coroutines.launch

class ActivityLoyaltyCards : AppCompatActivity() {

    private val TAG = "ActivityLoyaltyCards"

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoyaltyCardAdapter
    private lateinit var emptyView: TextView // Para mostrar el mensaje de vacío
    private val viewModel: ViewModelLoyaltyCards by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_cards)

        Log.d(TAG, "ActivityLoyaltyCards onCreate called")

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        emptyView = findViewById(R.id.empty_view)

        // Simulación de IDs de prueba
        val uniandesMemberId = "mOD7RaYRy6ew0wOwznvs"
        val businessOwnerId = "UqD7b4Twit3rD98w6Inq"
        val storeId = "olNh6XZeAVdRxgEHawJV"

        // Llamar a la función en el ViewModel para simular la creación o actualización de la tarjeta
        //viewModel.simulateLoyaltyCardCreation(businessOwnerId, uniandesMemberId, storeId, maxPoints = 8)



        loadLoyaltyCardsAndStores(uniandesMemberId)

        val optionsButton = findViewById<ImageButton>(R.id.options_image_view2)
        optionsButton.setOnClickListener {
            onBackPressed()
        }

    }

    private fun loadLoyaltyCardsAndStores(uniandesMemberId: String) {
        val loyaltyCardsLiveData = viewModel.getLoyaltyCardsForUser(uniandesMemberId)

        loyaltyCardsLiveData.observe(this@ActivityLoyaltyCards) { cards ->
            val loyaltyCards = cards ?: emptyList()
            Log.d(TAG, "Loyalty cards size: ${loyaltyCards.size}")

            lifecycleScope.launch {
                val fetchedStores = RepositoryStore.instance.getAllStores()
                    .filter { it.id != null }
                    .associateBy { it.id!! }

                if (loyaltyCards.isNotEmpty()) {
                    adapter = LoyaltyCardAdapter(loyaltyCards, fetchedStores) { selectedCard ->
                        // Aquí es donde se maneja el clic en la tarjeta seleccionada
                        Log.d(TAG, "Card clicked: ${selectedCard.id}")

                        // Inicia ActivityLoyaltyCardDetail con los datos correspondientes
                        val intent = Intent(this@ActivityLoyaltyCards, ActivityLoyaltyCardDetail::class.java).apply {
                            putExtra(LoyaltyCardAdapter.EXTRA_STORE_NAME, fetchedStores[selectedCard.storeId]?.name ?: "Tienda Desconocida")
                            putExtra(LoyaltyCardAdapter.EXTRA_STORE_ADDRESS, fetchedStores[selectedCard.storeId]?.address ?: "Dirección Desconocida")
                            putExtra(LoyaltyCardAdapter.EXTRA_STORE_IMAGE, fetchedStores[selectedCard.storeId]?.image)
                            putExtra(LoyaltyCardAdapter.EXTRA_POINTS, selectedCard.points)
                            putExtra(LoyaltyCardAdapter.EXTRA_MAX_POINTS, selectedCard.maxPoints)
                        }
                        startActivity(intent)
                    }

                    recyclerView.adapter = adapter
                    emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    // Código para mostrar un toast o cualquier otra lógica
                } else {
                    Log.d(TAG, "No hay tarjetas de lealtad para mostrar.")
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }
        }
    }


    private fun showClosestStoreToast(loyaltyCards: List<LoyaltyCard>, fetchedStores: Map<String, Store>) {
        val incompleteCards = loyaltyCards.filter { it.points < it.maxPoints }
        if (incompleteCards.isNotEmpty()) {
            val topCard = incompleteCards[0]
            val store = fetchedStores[topCard.storeId]
            val storeName = store?.name ?: "Desconocido"
            val pointsInfo = "${topCard.points} / ${topCard.maxPoints}"

            val toast = Toast.makeText(
                this,
                "Restaurante más cercano a llenar la tarjeta: $storeName ($pointsInfo)",
                Toast.LENGTH_SHORT
            )

            val durationInMilliseconds = 7000
            val handler = android.os.Handler(mainLooper)
            val delay: Long = 1000
            var timesShown = 0
            val runnable = object : Runnable {
                override fun run() {
                    if (timesShown * delay < durationInMilliseconds) {
                        toast.show()
                        timesShown++
                        handler.postDelayed(this, delay)
                    }
                }
            }
            handler.post(runnable)
        }
    }
}

