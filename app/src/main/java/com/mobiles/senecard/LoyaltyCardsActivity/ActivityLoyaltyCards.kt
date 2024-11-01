package com.mobiles.senecard.LoyaltyCardsActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var emptyView: TextView
    private val viewModel: ViewModelLoyaltyCards by viewModels()

    // Lanzador para ActivityResult
    private val detailActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Recargar las tarjetas de lealtad al regresar
            loadLoyaltyCardsAndStores()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_cards)

        Log.d(TAG, "ActivityLoyaltyCards onCreate called")

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        emptyView = findViewById(R.id.empty_view)

        // Simulación de ID de usuario
        val uniandesMemberId = "mOD7RaYRy6ew0wOwznvs"

        loadLoyaltyCardsAndStores()

        val optionsButton = findViewById<ImageButton>(R.id.options_image_view2)
        optionsButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadLoyaltyCardsAndStores() {
        val loyaltyCardsLiveData = viewModel.getLoyaltyCardsForUser()

        loyaltyCardsLiveData.observe(this@ActivityLoyaltyCards) { cards ->
            val loyaltyCards = cards ?: emptyList()
            Log.d(TAG, "Loyalty cards size: ${loyaltyCards.size}")

            lifecycleScope.launch {
                val fetchedStores = RepositoryStore.instance.getAllStores()
                    .filter { it.id != null }
                    .associateBy { it.id!! }

                // Filtrar solo las tarjetas incompletas
                val incompleteCards = loyaltyCards.filter { it.points < it.maxPoints }

                if (incompleteCards.isNotEmpty()) {
                    // Mostrar Toast para el primer negocio incompleto
                    val firstIncompleteCard = incompleteCards.firstOrNull()
                    firstIncompleteCard?.let { card ->
                        val storeName = fetchedStores[card.storeId]?.name ?: "Tienda Desconocida"
                        val pointsRemaining = card.maxPoints - card.points
                        Toast.makeText(
                            this@ActivityLoyaltyCards,
                            "Negocio: $storeName, te faltan $pointsRemaining puntos para completar.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    adapter = LoyaltyCardAdapter(loyaltyCards, fetchedStores) { selectedCard ->
                        Log.d(TAG, "Card clicked: ${selectedCard.id}")

                        val intent = Intent(this@ActivityLoyaltyCards, ActivityLoyaltyCardDetail::class.java).apply {
                            putExtra(LoyaltyCardAdapter.EXTRA_STORE_NAME, fetchedStores[selectedCard.storeId]?.name ?: "Tienda Desconocida")
                            putExtra(LoyaltyCardAdapter.EXTRA_STORE_ADDRESS, fetchedStores[selectedCard.storeId]?.address ?: "Dirección Desconocida")
                            putExtra(LoyaltyCardAdapter.EXTRA_STORE_IMAGE, fetchedStores[selectedCard.storeId]?.image)
                            putExtra(LoyaltyCardAdapter.EXTRA_POINTS, selectedCard.points)
                            putExtra(LoyaltyCardAdapter.EXTRA_MAX_POINTS, selectedCard.maxPoints)
                        }
                        detailActivityLauncher.launch(intent) // Usar el launcher para iniciar la actividad
                    }

                    recyclerView.adapter = adapter
                    emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                } else {
                    Log.d(TAG, "No hay tarjetas incompletas para mostrar.")
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }
        }
    }

}


