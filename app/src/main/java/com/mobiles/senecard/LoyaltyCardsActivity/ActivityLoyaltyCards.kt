package com.mobiles.senecard.LoyaltyCardsActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiles.senecard.LoyaltyCardsActivity.ActivityLoyaltyCardDetail.ActivityLoyaltyCardDetail
import com.mobiles.senecard.R
import com.mobiles.senecard.ViewModelLoyaltyCards
import com.mobiles.senecard.adapters.LoyaltyCardAdapter
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.launch

class ActivityLoyaltyCards : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoyaltyCardAdapter
    private lateinit var emptyView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val viewModel: ViewModelLoyaltyCards by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_cards)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        emptyView = findViewById(R.id.empty_view)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)

        // Configurar SwipeRefreshLayout
        setupSwipeRefresh()

        // Cargar datos al iniciar
        loadLoyaltyCardsAndStores()

        val optionsButton = findViewById<ImageButton>(R.id.options_image_view2)
        optionsButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            loadLoyaltyCardsAndStores(refresh = true)
        }
    }

    private fun loadLoyaltyCardsAndStores(refresh: Boolean = false) {
        lifecycleScope.launch {
            val currentUserId = viewModel.getCurrentUserId(this@ActivityLoyaltyCards)

            if (currentUserId != null) {
                if (refresh || isNetworkAvailable()) {
                    // Si hay red o estamos forzando un refresh, obtener datos del servidor
                    viewModel.fetchLoyaltyCardsForUser(currentUserId)
                    viewModel.loyaltyCards.observe(this@ActivityLoyaltyCards) { cards ->
                        saveLoyaltyCardsToPreferences(currentUserId, cards ?: emptyList())
                        displayLoyaltyCards(cards ?: emptyList())
                    }
                } else {
                    // Cargar desde local storage si no hay red
                    val localData = loadLoyaltyCardsFromPreferences()
                    val localUserId = localData.first
                    val localLoyaltyCards = localData.second

                    if (currentUserId == localUserId) {
                        if (localLoyaltyCards.isNotEmpty()) {
                            Toast.makeText(this@ActivityLoyaltyCards, "Mostrando datos desde almacenamiento local", Toast.LENGTH_SHORT).show()
                            displayLoyaltyCards(localLoyaltyCards)
                        } else {
                            Toast.makeText(this@ActivityLoyaltyCards, "No hay datos en almacenamiento local", Toast.LENGTH_LONG).show()
                            emptyView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        }
                    } else {
                        // Si el local storage no corresponde al usuario actual, pedir al usuario recargar
                        Toast.makeText(this@ActivityLoyaltyCards, "Los datos del almacenamiento local no corresponden al usuario actual", Toast.LENGTH_LONG).show()
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                }
            } else {
                Toast.makeText(this@ActivityLoyaltyCards, "No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            }

            swipeRefreshLayout.isRefreshing = false // Detener animación de carga
        }
    }

    private fun displayLoyaltyCards(loyaltyCards: List<LoyaltyCard>) {
        lifecycleScope.launch {
            val fetchedStores = RepositoryStore.instance.getAllStores()
                .filter { it.id != null }
                .associateBy { it.id!! }

            adapter = LoyaltyCardAdapter(loyaltyCards, fetchedStores) { selectedCard ->
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
            emptyView.visibility = if (loyaltyCards.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (loyaltyCards.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun saveLoyaltyCardsToPreferences(userId: String, cards: List<LoyaltyCard>) {
        val sharedPreferences = getSharedPreferences("local_storage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(cards)
        editor.putString("loyalty_cards", json)
        editor.putString("user_id", userId) // Guardar el ID del usuario asociado al local storage
        editor.apply()
    }

    private fun loadLoyaltyCardsFromPreferences(): Pair<String?, List<LoyaltyCard>> {
        val sharedPreferences = getSharedPreferences("local_storage", Context.MODE_PRIVATE)
        val storedUserId = sharedPreferences.getString("user_id", null)
        val json = sharedPreferences.getString("loyalty_cards", null)
        val loyaltyCards: List<LoyaltyCard> = if (json != null) {
            try {
                val type = object : TypeToken<List<LoyaltyCard>>() {}.type
                Gson().fromJson<List<LoyaltyCard>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList() // En caso de error de deserialización, retorna lista vacía
            }
        } else {
            emptyList()
        }
        return Pair(storedUserId, loyaltyCards)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    // Limpiar almacenamiento local al cerrar sesión
    fun clearLocalStorageOnLogout() {
        val sharedPreferences = getSharedPreferences("local_storage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("loyalty_cards")
        editor.remove("user_id")
        editor.apply()
    }
}
