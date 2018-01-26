//package pl.devone.android.mapboxexampleapp.services
//
//import com.google.android.gms.tasks.OnCompleteListener
//import com.google.firebase.auth.AuthResult
//import com.google.firebase.auth.FirebaseAuth
//
///**
// * Created by ljedrzynski on 14.01.2018.
// */
//object AuthService {
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
//
//    fun getCurrentUser() = auth.currentUser
//
//    fun isLoggedIn() = getCurrentUser() != null
//
//    fun signIn(email: String, password: String, onComplete: OnCompleteListener<AuthResult>) =
//            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(onComplete)
//}