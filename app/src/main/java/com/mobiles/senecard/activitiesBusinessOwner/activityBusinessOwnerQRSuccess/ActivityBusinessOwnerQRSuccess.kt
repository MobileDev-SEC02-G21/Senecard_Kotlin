import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.R

class ActivityBusinessOwnerQRSuccess : AppCompatActivity() {

    private val viewModel: ViewModelBusinessOwnerQRSuccess by viewModels()

    private var businessOwnerId: String? = null
    private var storeId: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_qr_success)

        // Retrieve the businessOwnerId, storeId, and userId passed from the previous activity
        businessOwnerId = intent.getStringExtra("businessOwnerId")
        storeId = intent.getStringExtra("storeId")
        userId = intent.getStringExtra("userId")

        // Set up observer to watch for purchase success or error
        observeViewModel()

        // Button to make the stamp (create purchase and update loyalty card)
        findViewById<Button>(R.id.makeStampButton).setOnClickListener {
            if (businessOwnerId != null && storeId != null && userId != null) {
                viewModel.makeStamp(businessOwnerId!!, storeId!!, userId!!)
            } else {
                Toast.makeText(this, "Missing required data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.purchaseSuccess.observe(this) { success ->
            if (success == true) {
                Toast.makeText(this, "Purchase registered successfully", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}
