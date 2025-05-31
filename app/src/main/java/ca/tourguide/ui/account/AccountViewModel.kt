package ca.tourguide.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class AccountViewModel : ViewModel() {
    private val _title = MutableLiveData<String>().apply {
        value = "Welcome to TourGuide! Please log in."
    }
    val title: LiveData<String> = _title

    private val _isLoggedIn = MutableLiveData<Boolean>().apply{
        value = false
    }
    val isLoggedIn = _isLoggedIn

    private val _userInfo = MutableLiveData<UserInfo?>()
    val userInfo: LiveData<UserInfo?> = _userInfo

    fun updateLoginState(user: FirebaseUser?) {
        if (user != null) {
            _isLoggedIn.value = true
            _title.value = "Welcome, ${user.displayName ?: "user"}!"
            _userInfo.value = UserInfo(
                displayName = user.displayName,
                email = user.email,
                photoUri = user.photoUrl?.toString()
            )
        } else {
            _isLoggedIn.value = false
            _title.value = "Welcome to TourGuide! Please log in."
            _userInfo.value = null
        }
    }

}