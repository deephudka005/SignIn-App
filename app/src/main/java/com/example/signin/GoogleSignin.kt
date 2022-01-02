package com.example.signin

import android.R.attr
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_google_signin.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import android.R.attr.data
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.signin.dao.UserDao
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import com.facebook.GraphResponse

import org.json.JSONObject

import com.facebook.GraphRequest
import com.facebook.AccessToken
import com.google.common.graph.Graph
import com.google.firebase.auth.*
import java.util.*
import com.google.firebase.auth.AuthCredential as AuthCredential

class GoogleSignin : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private lateinit var callbackManager: CallbackManager
    private lateinit var buttonFacebookLogin: LoginButton
    private var RC_SIGN_IN= 89
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_signin)



        //Facebook login button initialise
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        buttonFacebookLogin=findViewById(R.id.login_button)
            callbackManager = CallbackManager.Factory.create()

            buttonFacebookLogin.setReadPermissions("email", "public_profile")
            buttonFacebookLogin.registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    //Log.d("FIRE90", "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken)
                    //For getting data from facebook
                    val graphRequest = GraphRequest.newMeRequest(loginResult?.accessToken){obj , response->

                    }
                    val param=Bundle()
                    param.putString("fields","name,email,id,picture.type(large)")
                    graphRequest.parameters= param
                    graphRequest.executeAsync()
                }

                override fun onCancel() {
                    Log.d("FIRE90", "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.d("FIRE90", "facebook:onError", error)
                }
            })

        //.......
        // GOOGLE SIGNIN
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id_auth))
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth= Firebase.auth
        signInButton.setOnClickListener {
            signIn()
        }
        //..........

    }
    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(account: FirebaseUser?) {
        if(account!= null){

            val user= com.example.signin.model.User(
                account.uid,
                account.displayName,
                account.photoUrl.toString(),
                account.email.toString(),
                account.phoneNumber.toString(),
                //account.gender.toString(),
                //account.last_name.toString()

            )
            val usersDao= UserDao()
            usersDao.addUser(user)

            val profileActivityIntent= Intent(this, ProfilePage::class.java)
            //profileActivityIntent.putExtra(ProfilePage.KEY_ONE,account.displayName)
            //profileActivityIntent.putExtra(ProfilePage.KEY_TWO,account.photoUrl.toString())
            //profileActivityIntent.putExtra(ProfilePage.KEY_THREE,account.email.toString())
            //profileActivityIntent.putExtra(ProfilePage.KEY_FOUR,account.phoneNumber.toString())
            //profileActivityIntent.putExtra(ProfilePage.KEY_FIVE,account.last_name.toString())
            //profileActivityIntent.putExtra(ProfilePage.KEY_SIX,account.gender.toString())
            startActivity(profileActivityIntent)
            finish()
        }
        else{
            signInButton.visibility= View.VISIBLE
            progressBar.visibility= View.GONE
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        //getResult.launch(signInIntent)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    /*private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleSignInResult(task);
        }
    }*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("FIRE89", "Google sign in failed", e)
            }
        }

        // Pass the activity result back to the Facebook SDK

    }


    // [START auth_with_facebook]

    private fun handleFacebookAccessToken(token: AccessToken) {

        val credential = FacebookAuthProvider.getCredential(token.token)
        login_button.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("FACEBOOK","signInWithCredential:success")
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    updateUI(null)

                }
            }
    }
    // [END auth_with_facebook]


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        signInButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FIRE89", "signInWithCredential:success")

                    val user = auth.currentUser
                    //Log.d("FIRE89","firebaseAuthWithGoogle: ${user?.displayName}")
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FIRE89", "signInWithCredential:failure",task.exception)
                    updateUI(null)
                }
            }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 0
    }
}