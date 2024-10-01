package com.mobiles.senecard.activityHomeUniandesMember

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.mobiles.senecard.activitiesInitial.activityInitial.ActivityInitial
import com.mobiles.senecard.databinding.ActivityHomeUniandesMemberBinding

class ActivityHomeUniandesMember : AppCompatActivity() {

    private lateinit var binding: ActivityHomeUniandesMemberBinding
    private val viewModelHomeUniandesMember: ViewModelHomeUniandesMember by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeUniandesMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
        setElementsMenu()
        setObserversMenu()
        viewModelHomeUniandesMember.validateSession()
    }

    private fun setElements() {
        binding.optionsImageView.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setObservers() {
        viewModelHomeUniandesMember.isLogged.observe(this) { logged ->
            if (!logged) {
                val initialIntent = Intent(this, ActivityInitial::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(initialIntent)
            }
        }

    }
    
    private fun setElementsMenu() {
        binding.backImageView.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.homeButton.setOnClickListener {
            Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show()
        }

        binding.qrCodeButton.setOnClickListener {
            Toast.makeText(this, "Código QR", Toast.LENGTH_SHORT).show()
        }

        binding.loyaltyCardsButton.setOnClickListener {
            Toast.makeText(this, "Tarjetas de Fidelización", Toast.LENGTH_SHORT).show()
        }

        binding.profileButton.setOnClickListener {
            Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
        }

        binding.logOutButton.setOnClickListener {
            viewModelHomeUniandesMember.logOut()
        }
    }

    private fun setObserversMenu() {
        viewModelHomeUniandesMember.isLoggedOut.observe(this) { isLoggedOut ->
            if (isLoggedOut) {
                val initialIntent = Intent(this, ActivityInitial::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(initialIntent)
            }
        }

    }
}