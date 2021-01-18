package com.example.android.storemanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.android.storemanagement.firebase.FirebaseDatabaseOperations.addFirebaseUser
import com.example.android.storemanagement.firebase.FirebaseUserInternal
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.LoginStatusCallback
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*


class ActivityLogin : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton: LoginButton
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Initialize Firebase Auth
        auth = Firebase.auth

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(server_client_id)
                .requestIdToken(server_client_id)
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        if (auth.currentUser == null) {
            googleSignInClient.signOut()
            LoginManager.getInstance().logOut()
        }
        // Set the dimensions of the sign-in button.
        sign_in_button.setSize(SignInButton.SIZE_STANDARD)

        sign_in_button.setOnClickListener {
            signIn()
        }

        buttonContinue.setOnClickListener {
            openMainActivity()
        }

        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        callbackManager = CallbackManager.Factory.create()
        loginButton = findViewById(R.id.facebook_login_button)
//        loginButton.setReadPermissions("email", "public_profile", "user_friends")
        loginButton.setPermissions(listOf(EMAIL))

        loginButton.setOnClickListener {
            if (!isLoggedIn) {
                LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"))
            }
        }

        // Callback registration
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                Log.d("LoginActivity", "facebook success $loginResult")
                handleFacebookAccessToken(loginResult?.accessToken!!)
            }

            override fun onCancel() {
                Log.d("LoginActivity", "facebook cancelled")
                // App code
            }

            override fun onError(exception: FacebookException) {
                Log.d("LoginActivity", "facebook error $exception")
                // App code
            }
        })

        LoginManager.getInstance().retrieveLoginStatus(this, object : LoginStatusCallback {
            override fun onCompleted(accessToken: AccessToken) {
                // User was previously logged in, can log them in directly here.
                // If this callback is called, a popup notification appears that says
                // "Logged in as <User Name>"
                Log.d("LoginActivity", "facebook onCompleted $accessToken")
            }

            override fun onFailure() {
                // No access token could be retrieved for the user
                Log.d("LoginActivity", "facebook onFailure")
            }

            override fun onError(exception: Exception) {
                // An error occurred
                Log.d("LoginActivity", "facebook onError $exception")
            }
        })

        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("LoginActivity", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LoginActivity", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }

                // ...
            }
    }

    private fun openAccountTypeSelectionActivity(fbUserId: String) {
        val intent = Intent(this, ActivityAccountTypeSelection::class.java)
        intent.putExtra("fbUserId", fbUserId)
        startActivity(intent)
        finish()
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun signIn() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("LoginActivity", "Google sign in failed", e)
            }
        }

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            // The Task returned from this call is always completed, no need to attach
//            // a listener.
//            val task =
//                GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleSignInResult(task)
//        }
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LoginActivity", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    // ...
                    Toast.makeText(this, "Authentication Failed.", LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
//        try {
//            val account = completedTask.getResult(ApiException::class.java)
//
//            Log.d("Tina", "account $account")
//            // Signed in successfully, show authenticated UI.
//            updateUI(account)
//        } catch (e: ApiException) {
//            // The ApiException status code indicates the detailed failure reason.
//            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            Log.w("FragmentActivity", "signInResult:failed code=" + e.statusCode)
//            updateUI(null)
//        }
    }

    private fun updateUI(account: FirebaseUser?) {
        Log.d("Tina", "updateUI account $account")
        email_text.setText(" ")
        if (account != null) email_text.setText(account.email)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is signed in
            Log.d("Tina", "signed in $account")
            addFirebaseUser { fbUserId ->
                if (fbUserId != null) {
                    checkFirebaseUserType(fbUserId)
                } else openMainActivity()
            }
        } else {
            // No user is signed in
            Log.d("Tina", "no user signed in")
        }
    }

    private fun checkFirebaseUserType(fbUserId: String) {
//        val user = FirebaseAuth.getInstance().currentUser
//        val uniqueId: String = user!!.uid
//        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
//        val myRef: Query = database.getReference("Users")
//        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val users = dataSnapshot.children
//                for (user in users) {
//                    val firebaseUser = user.getValue(FirebaseUserInternal::class.java)
//                    if (firebaseUser?.id == fbUserId) {
//                        Log.d("UserTina", "fbUser " + firebaseUser.id + " curr " + uniqueId)
//                        if (firebaseUser.accountType.isBlank()) {
//                            Log.d("UserTina", " type " + firebaseUser.accountType)
//                            runOnUiThread { openAccountTypeSelectionActivity(fbUserId) }
//                        } else {
//                            runOnUiThread { openMainActivity() }
//                        }
//                    }
//                }
//            }
        val user = FirebaseAuth.getInstance().currentUser
        val uniqueId: String = user!!.uid
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val myRef: Query = database.getReference("Users").orderByChild("id").equalTo(uniqueId)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firebaseUser = dataSnapshot.getValue(FirebaseUserInternal::class.java)
                Log.d("UserTina", "fbUser " + firebaseUser?.id + " curr " + uniqueId)
                if (firebaseUser != null && firebaseUser.accountType.isBlank()) {
                    runOnUiThread { openAccountTypeSelectionActivity(fbUserId) }
                } else {
                    runOnUiThread { openMainActivity() }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun openFragment(loginFragment: Fragment, tag: String) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.login_fragment_container, loginFragment, tag)
        transaction.setCustomAnimations(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        transaction.addToBackStack("a")
        transaction.commit()
    }

    companion object {
        private const val server_client_id =
            "191395528817-1posttkm7jhstrnk827s1d2h8enc7jla.apps.googleusercontent.com"
        private const val EMAIL = "email"
    }
}
