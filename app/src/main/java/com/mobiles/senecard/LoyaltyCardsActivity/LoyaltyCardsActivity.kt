package com.mobiles.senecard.LoyaltyCardsActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobiles.senecard.LoyaltyCardsActivity.LoyaltyBusinessCardActivity.LoyaltyBusinessCardActivity
import com.mobiles.senecard.R
import com.mobiles.senecard.adapters.LoyaltyCardAdapter
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.Store
import kotlinx.coroutines.launch

class ActivityLoyaltyCards : AppCompatActivity() {

    private val TAG = "ActivityLoyaltyCards"

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoyaltyCardAdapter
    private lateinit var emptyView: TextView // Para mostrar el mensaje de vacío

    // Crear una instancia del ViewModel
    private val viewModel: LoyaltyCardsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_cards)

        Log.d(TAG, "ActivityLoyaltyCards onCreate called")

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar el TextView para el mensaje vacío
        emptyView = findViewById(R.id.empty_view)

        val loyaltyCardsContainer = findViewById<ConstraintLayout>(R.id.loyalty_cards_container)

        // Simulación de IDs de prueba
        val businessOwnerId = "UqD7b4Twit3rD98w6Inq"
        val uniandesMemberId = "Z1aNZn8BnA9dxVdT9QaK"
        val storeId = "olNh6XZeAVdRxgEHawJV"

        // Cargar tarjetas de lealtad y tiendas
        loadLoyaltyCardsAndStores(uniandesMemberId)

        // Cuando se haga clic en el contenedor, intenta crear o actualizar la RoyaltyCard
        loyaltyCardsContainer.setOnClickListener {
            Log.d(TAG, "Loyalty cards container clicked")

            // Llamar a la función en el ViewModel para simular la creación o actualización de la tarjeta
            viewModel.simulateRoyaltyCardCreation(businessOwnerId, uniandesMemberId, storeId, maxPoints = 8)

            // Obtener información de la tienda usando el storeId
            getStoreDetails(storeId)

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

    private fun loadLoyaltyCardsAndStores(uniandesMemberId: String) {
        Log.d(TAG, "Entrando a loadLoyaltyCardsAndStores.")

        // Obtener el LiveData de tarjetas de lealtad
        val loyaltyCardsLiveData = viewModel.getLoyaltyCardsForUser(uniandesMemberId)

        // Observar los cambios en el LiveData
        loyaltyCardsLiveData.observe(this@ActivityLoyaltyCards) { cards ->
            val loyaltyCards = cards ?: emptyList() // Manejar el caso de no encontrar tarjetas
            Log.d(TAG, "Loyalty cards size: ${loyaltyCards.size}") // Log para verificar cantidad

            // Obtener tiendas de forma sincrónica, si es necesario
            lifecycleScope.launch {
                val fetchedStores = RepositoryStore.instance.getAllStores()
                    .filter { it.id != null } // Filtrar IDs nulos
                    .associateBy { it.id!! } // Asociar por ID

                // Actualizar el RecyclerView
                if (loyaltyCards.isNotEmpty()) {
                    adapter = LoyaltyCardAdapter(loyaltyCards, fetchedStores) { selectedCard ->
                        Log.d(TAG, "Card clicked: ${selectedCard.id}")
                    }
                    recyclerView.adapter = adapter // Asignar el adaptador al RecyclerView
                    emptyView.visibility = View.GONE // Ocultar mensaje vacío
                    recyclerView.visibility = View.VISIBLE // Mostrar RecyclerView
                } else {
                    Log.d(TAG, "No hay tarjetas de lealtad para mostrar.")
                    emptyView.visibility = View.VISIBLE // Mostrar mensaje vacío
                    recyclerView.visibility = View.GONE // Ocultar RecyclerView
                }
            }
        }
    }

    private fun getStoreDetails(storeId: String) {
        // Usar corutinas para obtener datos de Firestore
        lifecycleScope.launch {
            val store = RepositoryStore.instance.getStoreById(storeId)
            if (store != null) {
                Log.d(TAG, "Store Name: ${store.name}")
                Log.d(TAG, "Store Image: ${store.image}")
            } else {
                Log.d(TAG, "No se encontró la tienda con ID: $storeId")
            }
        }
    }
}
