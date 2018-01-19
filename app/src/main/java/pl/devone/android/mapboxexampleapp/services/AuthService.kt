package pl.devone.android.mapboxexampleapp.services

import com.google.firebase.auth.FirebaseAuth

/**
 * Created by ljedrzynski on 14.01.2018.
 */
class AuthService {

    companion object {
        private val authenticator: FirebaseAuth = FirebaseAuth.getInstance()

        fun getCurrentUser() = authenticator.currentUser

        fun isLoggedIn() = getCurrentUser() != null
    }
}