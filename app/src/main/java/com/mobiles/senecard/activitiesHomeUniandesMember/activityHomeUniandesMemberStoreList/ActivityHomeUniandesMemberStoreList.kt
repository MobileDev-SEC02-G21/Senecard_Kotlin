package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreList

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobiles.senecard.R
import com.mobiles.senecard.StoreAdapter
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember.ActivityHomeUniandesMember
import com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreDetail.ActivityHomeUniandesMemberStoreDetail
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberStoreListBinding

class ActivityHomeUniandesMemberStoreList : AppCompatActivity() {

    private lateinit var binding: ActivityHomeUniandesMemberStoreListBinding
    private val viewModelHomeUniandesMemberStoreList: ViewModelHomeUniandesMemberStoreList by viewModels()

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
            binding.storeRecyclerView.adapter = StoreAdapter(stores) { store ->
                viewModelHomeUniandesMemberStoreList.onClickedItemStore(store)
            }
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
}