package com.tollview.tollview.ui.login

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    val launcher = rememberLauncherForActivityResult(StartActivityForResult()) { result: ActivityResult ->
        Log.d("LOGINCHECK", "Activity result received")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.d("LOGINCHECK", "Google sign-in successful: ${account.email}")
                firebaseAuthWithGoogle(account.idToken, auth, onLoginSuccess)
            } else {
                Log.d("LOGINCHECK", "Google sign-in account is null")
            }
        } catch (e: ApiException) {
            Log.e("LOGINCHECK", "Google sign-in failed: ${e.localizedMessage}", e)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            try {
                Log.d("LOGINCHECK", "Sign-in button clicked")
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("536066125827-qu8eg2se506s2428n9k1brc6vskl5b3t.apps.googleusercontent.com")
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                Log.d("LOGINCHECK", "Launching sign-in intent")
                launcher.launch(googleSignInClient.signInIntent)
            } catch (e: Exception) {
                Log.e("LOGINCHECK", "Sign-in failed: ${e.localizedMessage}", e)
            }
        }) {
            Text(text = "Sign in with Google")
        }
    }
}

private fun firebaseAuthWithGoogle(idToken: String?, auth: FirebaseAuth, onLoginSuccess: () -> Unit) {
    if (idToken == null) {
        Log.e("YEAHTHISONE", "Google ID Token is null")
        return
    }
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("YEAHTHISONE", "Firebase authentication successful: ${auth.currentUser?.email}")
                onLoginSuccess()
            } else {
                Log.e("YEAHTHISONE", "Firebase authentication failed", task.exception)
            }
        }
}