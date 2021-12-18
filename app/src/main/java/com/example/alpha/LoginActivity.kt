package com.example.alpha

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    //create a button variable
    lateinit var btnSignIn:com.google.android.gms.common.SignInButton
    private lateinit var auth: FirebaseAuth

    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide() //themes: hide menuBar

        auth = Firebase.auth

        //assign the xml view to the button
        btnSignIn= findViewById(R.id.btnSignIn)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //now set click listener for button
        btnSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            resultLauncher.launch(signInIntent) //startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }
    }
    //on activity result alternative
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                if (task.isSuccessful) {
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        //Google Sign In failed, update UI appropriately
                        Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    // Send user to main
                    gotomain()

                    saveUserDataToDatabase(task.result!!.user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "failed to sign in", Toast.LENGTH_SHORT).show()
                }
            }
    }
    fun gotomain()
    {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
    }
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser!=null)
        {
            //user exists so direct him to another activity
            gotomain()
        }
    }

    // save user data to database
    fun saveUserDataToDatabase(user : FirebaseUser?){
        val email = user?.email
        val uid = user?.uid

        var userDTO = UserDTO()
        userDTO.email = email
        userDTO.emoji =  "\uD83D\uDC3C \uD83C\uDF3F \uD83D\uDDA4 "

        FirebaseFirestore.getInstance().collection("users").document(uid!!).set(userDTO)
        //
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }
}