package com.example.timetabler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class LoginScreen : AppCompatActivity() {

    private lateinit var emailBox : TextInputEditText
    private lateinit var passwordBox : TextInputEditText
    private lateinit var loginBtn : TextView
    private lateinit var forgotPasswordBtn : TextView

    private val db = FirebaseFirestore.getInstance()
    private val inst = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth;

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        emailBox = findViewById(R.id.email)
        passwordBox = findViewById(R.id.password)
        loginBtn = findViewById(R.id.login)
        forgotPasswordBtn = findViewById(R.id.forgotPassword)
        auth = Firebase.auth


        if(auth.currentUser != null)
        {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginBtn.setOnClickListener{
            if(!emailBox.text.toString().isNullOrEmpty() && !passwordBox.text.toString().isNullOrEmpty())
            {
                auth.signInWithEmailAndPassword(emailBox.text.toString(), passwordBox.text.toString()).addOnSuccessListener{
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener{
                    Toast.makeText(this, "Email or Password Incorrect", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this, "Please enter an email and/or password", Toast.LENGTH_SHORT).show()
            }

        }
        forgotPasswordBtn.setOnClickListener {
            val email = emailBox.text.toString().trim() // Trim whitespace

            if (!email.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email).addOnSuccessListener {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}