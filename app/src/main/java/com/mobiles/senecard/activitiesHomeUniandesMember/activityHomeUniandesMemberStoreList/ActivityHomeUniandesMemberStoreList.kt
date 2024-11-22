package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreList

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobiles.senecard.R
import com.mobiles.senecard.StoreAdapter
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember.ActivityHomeUniandesMember
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreDetail.ActivityHomeUniandesMemberStoreDetail
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberStoreListBinding

class ActivityHomeUniandesMemberStoreList : AppCompatActivity() {

    private lateinit var binding: ActivityHomeUniandesMemberStoreListBinding
    private val viewModelHomeUniandesMemberStoreList: ViewModelHomeUniandesMemberStoreList by viewModels()
    private var selectedCategoryButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeUniandesMemberStoreListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
        viewModelHomeUniandesMemberStoreList.getAllStores()
    }

    private fun setElements() {
        binding.storeRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModelHomeUniandesMemberStoreList.filterStoresByName(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.backImageView.setOnClickListener {
            viewModelHomeUniandesMemberStoreList.backImageViewClicked()
        }
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.white)
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.primary))
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModelHomeUniandesMemberStoreList.getAllStores()
        }
    }

    private fun setObservers() {
        viewModelHomeUniandesMemberStoreList.navigateToActivityHomeUniandesMember.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ActivityHomeUniandesMember::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                startActivity(intent, options.toBundle())
                viewModelHomeUniandesMemberStoreList.onNavigated()
            }
        }
        viewModelHomeUniandesMemberStoreList.storeList.observe(this) { stores ->
            binding.loadingAnimation.visibility = View.GONE
            if (stores.isNotEmpty()) {
                binding.errorConnectionStores.visibility = View.GONE
                binding.messageNoConnectionStores.visibility = View.GONE
                viewModelHomeUniandesMemberStoreList.applyFilters()
            } else {
                binding.errorConnectionStores.visibility = View.VISIBLE
                binding.messageNoConnectionStores.visibility = View.VISIBLE
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
        viewModelHomeUniandesMemberStoreList.filteredStoreList.observe(this) { filteredStores ->
            binding.storeRecyclerView.adapter = StoreAdapter(filteredStores) { store ->
                viewModelHomeUniandesMemberStoreList.onClickedItemStore(store)
            }
        }
        viewModelHomeUniandesMemberStoreList.navigateToActivityHomeUniandesMemberStoreDetail.observe(this) { store ->
            if (store != null) {
                val intent = Intent(this, ActivityHomeUniandesMemberStoreDetail::class.java)
                intent.putExtra("storeId", store.id)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
                viewModelHomeUniandesMemberStoreList.onNavigated()
            }
        }
    }

    fun onCategoryClicked(view: View) {
        val clickedButton = view as Button
        val category = clickedButton.text.toString()

        if (selectedCategoryButton == clickedButton) {
            selectedCategoryButton?.setBackgroundResource(R.drawable.home_categories_scroll_button_background)
            selectedCategoryButton = null
            viewModelHomeUniandesMemberStoreList.filterStoresByCategory("All")
        } else {
            selectedCategoryButton?.setBackgroundResource(R.drawable.home_categories_scroll_button_background)
            clickedButton.setBackgroundResource(R.drawable.home_categories_scroll_button_selected_background)
            selectedCategoryButton = clickedButton
            viewModelHomeUniandesMemberStoreList.filterStoresByCategory(category)

        }
    }
}