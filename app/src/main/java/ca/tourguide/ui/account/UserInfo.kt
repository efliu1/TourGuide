package ca.tourguide.ui.account

import android.net.Uri
import androidx.core.net.toUri

data class UserInfo(
    val email: String?,
    val displayName: String?,
    val photoUri: String?
)