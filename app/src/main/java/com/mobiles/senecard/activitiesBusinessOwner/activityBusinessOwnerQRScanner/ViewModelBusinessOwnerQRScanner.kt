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

    private val repositoryUser = RepositoryUser.instance

    fun processQRCode(qrCode: String, businessOwnerId: String, storeId: String) {
        viewModelScope.launch {
            try {
                // Use RepositoryUser to check if the user exists in Firestore
                val user = repositoryUser.getUserById(qrCode)
                if (user != null) {
                    // If user exists, navigate to success
                    _navigateToSuccess.value = true
                } else {
                    // If user does not exist, navigate to failure
                    _navigateToFailure.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching user data: ${e.message}"
                _navigateToFailure.value = true
            }
        }
    }
}
