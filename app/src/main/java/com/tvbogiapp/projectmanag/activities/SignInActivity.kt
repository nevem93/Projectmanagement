package com.tvbogiapp.projectmanag.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.tvbogiapp.projectmanag.R
import com.tvbogiapp.projectmanag.firebase.FireStoreClass
import com.tvbogiapp.projectmanag.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }
    }

    fun signInSuccess(user: User){
        startActivity((Intent(this, MainActivity::class.java)))
        finish()


    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_in_activity)

        val actionBar = supportActionBar
        if( actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    private fun signInRegisteredUser(){
        val email: String = et_email_sign_in.text.toString().trim{ it <=' '}
        val password: String = et_password_sign_in.text.toString().trim{ it <=' '}
        
        if(validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        FireStoreClass().loadUserData(this@SignInActivity)
                        startActivity((Intent(this, MainActivity::class.java)))

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()

                    }

                }
        }
        
    }

    private fun validateForm(email: String, password: String): Boolean{
        return when {
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            } else-> { true}

        }
    }

}
