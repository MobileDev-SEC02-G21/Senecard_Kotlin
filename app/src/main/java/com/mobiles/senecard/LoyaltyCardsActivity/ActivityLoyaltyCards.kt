package com.mobiles.senecard.LoyaltyCardsActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Debug
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityLoyaltyCards : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val viewModel: ViewModelLoyaltyCards by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Debug.startMethodTracing("lifecycle_events");

        setContentView(R.layout.activity_loyalty_cards)

        initializeViews()
        setupSwipeRefresh()
        setupObservers()
        loadLoyaltyCardsAndStores()

        findViewById<ImageButton>(R.id.options_image_view2).setOnClickListener {
            onBackPressed()
        }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        emptyView = findViewById(R.id.empty_view)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            loadLoyaltyCardsAndStores(refresh = true)
        }
    }

    private fun setupObservers() {
        viewModel.loyaltyCards.observe(this) { cards ->
            lifecycleScope.launch {
                val userId = viewModel.getCurrentUserId(this@ActivityLoyaltyCards)
                if (userId != null) {
                    saveLoyaltyCardsToPreferences(userId, cards ?: emptyList())
                }
                displayLoyaltyCards(cards ?: emptyList())
            }
        }
    }

    private fun loadLoyaltyCardsAndStores(refresh: Boolean = false) {
        lifecycleScope.launch {
            val currentUserId = viewModel.getCurrentUserId(this@ActivityLoyaltyCards)
            if (currentUserId != null) {
                if (refresh || isNetworkAvailable()) {
                    viewModel.fetchLoyaltyCardsForUser(currentUserId)
                } else {
                    loadLocalData(currentUserId)
                }
            } else {
                showToast("No se pudo obtener el ID del usuario")
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private suspend fun loadLocalData(currentUserId: String) {
        val (localUserId, localLoyaltyCards) = loadLoyaltyCardsFromPreferences()
        if (currentUserId == localUserId && localLoyaltyCards.isNotEmpty()) {
            showToast("Mostrando datos desde almacenamiento local")
            displayLoyaltyCards(localLoyaltyCards)
        } else {
            handleEmptyLocalData(currentUserId != localUserId)
        }
    }

    private fun handleEmptyLocalData(isMismatchedUser: Boolean) {
        if (isMismatchedUser) {
            showToast("Los datos del almacenamiento local no corresponden al usuario actual")
        } else {
            showToast("No hay datos en almacenamiento local")
        }
        emptyView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private suspend fun saveLoyaltyCardsToPreferences(userId: String, cards: List<LoyaltyCard>) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = getSharedPreferences("local_storage", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putString("loyalty_cards", Gson().toJson(cards))
                putString("user_id", userId)
                apply()
            }
        }
    }

    private suspend fun loadLoyaltyCardsFromPreferences(): Pair<String?, List<LoyaltyCard>> {
        return withContext(Dispatchers.IO) {
            val sharedPreferences = getSharedPreferences("local_storage", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("user_id", null)
            val loyaltyCardsJson = sharedPreferences.getString("loyalty_cards", null)
            val loyaltyCards: List<LoyaltyCard> = if (!loyaltyCardsJson.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<LoyaltyCard>>() {}.type
                    Gson().fromJson(loyaltyCardsJson, type)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
            userId to loyaltyCards
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }

    // El método displayLoyaltyCards se mantiene intacto según tu requerimiento
    private fun displayLoyaltyCards(loyaltyCards: List<LoyaltyCard>) {
        lifecycleScope.launch {
            val fetchedStores = RepositoryStore.instance.getAllStores()
                .filter { it.id != null }
                .associateBy { it.id!! }

            var adapter = LoyaltyCardAdapter(loyaltyCards, fetchedStores) { selectedCard ->
                val intent = Intent(
                    this@ActivityLoyaltyCards,
                    ActivityLoyaltyCardDetail::class.java
                ).apply {
                    putExtra(
                        LoyaltyCardAdapter.EXTRA_STORE_NAME,
                        fetchedStores[selectedCard.storeId]?.name ?: "Tienda Desconocida"
                    )
                    putExtra(
                        LoyaltyCardAdapter.EXTRA_STORE_ADDRESS,
                        fetchedStores[selectedCard.storeId]?.address ?: "Dirección Desconocida"
                    )
                    putExtra(
                        LoyaltyCardAdapter.EXTRA_STORE_IMAGE,
                        fetchedStores[selectedCard.storeId]?.image
                    )
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
}
