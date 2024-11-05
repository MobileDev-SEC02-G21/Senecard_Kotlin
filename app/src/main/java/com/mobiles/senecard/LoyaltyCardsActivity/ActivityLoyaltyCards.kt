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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiles.senecard.LoyaltyCardsActivity.ActivityLoyaltyCardDetail.ActivityLoyaltyCardDetail
import com.mobiles.senecard.R
import com.mobiles.senecard.ViewModelLoyaltyCards
import com.mobiles.senecard.adapters.LoyaltyCardAdapter
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.LoyaltyCard
import com.mobiles.senecard.utils.UserSessionManager
import kotlinx.coroutines.launch

class ActivityLoyaltyCards : AppCompatActivity() {

    private val TAG = "ActivityLoyaltyCards"
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoyaltyCardAdapter
    private lateinit var emptyView: TextView

    private val viewModel: ViewModelLoyaltyCards by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loyalty_cards)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        emptyView = findViewById(R.id.empty_view)

        loadLoyaltyCardsAndStores()

        val optionsButton = findViewById<ImageButton>(R.id.options_image_view2)
        optionsButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadLoyaltyCardsAndStores() {
        lifecycleScope.launch {
            val currentUserId = viewModel.getCurrentUserId(this@ActivityLoyaltyCards)

            if (currentUserId != null) {
                // Verifica si el ID de usuario en caché coincide con el ID actual
                val cachedUserId = UserSessionManager.getUserId(this@ActivityLoyaltyCards)

                // Si el ID de usuario cambia, limpiamos el caché
                if (cachedUserId != currentUserId) {
                    clearLoyaltyCardsCache()  // Limpia el caché de las loyalty cards
                    UserSessionManager.saveUserId(this@ActivityLoyaltyCards, currentUserId)  // Actualiza el ID del usuario en caché
                }

                if (isNetworkAvailable()) {
                    // Con red disponible, obtenemos tarjetas de lealtad del servidor
                    viewModel.fetchLoyaltyCardsForUser(currentUserId)
                    viewModel.loyaltyCards.observe(this@ActivityLoyaltyCards) { cards ->
                        saveLoyaltyCardsToPreferences(cards ?: emptyList())
                        displayLoyaltyCards(cards ?: emptyList())
                    }
                } else {
                    // Sin red, carga tarjetas de lealtad desde caché si el ID coincide
                    val cachedLoyaltyCards = loadLoyaltyCardsFromPreferences()
                    if (cachedLoyaltyCards.isNotEmpty()) {
                        Toast.makeText(this@ActivityLoyaltyCards, "Mostrando datos en caché", Toast.LENGTH_SHORT).show()
                        displayLoyaltyCards(cachedLoyaltyCards)
                    } else {
                        Toast.makeText(this@ActivityLoyaltyCards, "No hay datos en caché disponibles", Toast.LENGTH_LONG).show()
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                }
            } else {
                Toast.makeText(this@ActivityLoyaltyCards, "No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            }
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
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun saveLoyaltyCardsToPreferences(cards: List<LoyaltyCard>) {
        val sharedPreferences = getSharedPreferences("loyalty_cache", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(cards)
        editor.putString("loyalty_cards", json)
        editor.apply()
    }

    private fun loadLoyaltyCardsFromPreferences(): List<LoyaltyCard> {
        val sharedPreferences = getSharedPreferences("loyalty_cache", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("loyalty_cards", null)
        return if (json != null) {
            val type = object : TypeToken<List<LoyaltyCard>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }

    private fun clearLoyaltyCardsCache() {
        val sharedPreferences = getSharedPreferences("loyalty_cache", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("loyalty_cards").apply()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}
