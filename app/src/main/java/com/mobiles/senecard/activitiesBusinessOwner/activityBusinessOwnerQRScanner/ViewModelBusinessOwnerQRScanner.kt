import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryUser
import kotlinx.coroutines.launch

class ViewModelBusinessOwnerQRScanner : ViewModel() {

    private val _navigateToSuccess = MutableLiveData<Boolean>()
    val navigateToSuccess: LiveData<Boolean> get() = _navigateToSuccess

    private val _navigateToFailure = MutableLiveData<Boolean>()
    val navigateToFailure: LiveData<Boolean> get() = _navigateToFailure

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _userId = MutableLiveData<String?>() // To hold the userId when the user exists
    val userId: LiveData<String?> get() = _userId

    private val repositoryUser = RepositoryUser.instance

    fun processQRCode(qrCode: String) {
        viewModelScope.launch {
            try {
                // Use RepositoryUser to check if the user exists in Firestore
                val user = repositoryUser.getUserById(qrCode)
                if (user != null) {
                    // If user exists, save the userId and navigate to success
                    _userId.value = user.id // Store the userId
                    _navigateToSuccess.value = true
                } else {
                    _navigateToFailure.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching user data: ${e.message}"
                _navigateToFailure.value = true
            }
        }
    }
}
