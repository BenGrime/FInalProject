package com.example.timetabler

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.Executor
import androidx.biometric.BiometricPrompt
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys


class LoginScreen : AppCompatActivity() {

    private lateinit var emailBox : TextInputEditText
    private lateinit var passwordBox : TextInputEditText
    private lateinit var loginBtn : TextView
    private lateinit var forgotPasswordBtn : TextView
    private lateinit var biometrics : TextView

    private val db = FirebaseFirestore.getInstance()
    private val inst = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth;

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
        biometrics = findViewById(R.id.biotemtic)
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
                    saveCredentials(this, emailBox.text.toString(), passwordBox.text.toString())
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
        biometrics.setOnClickListener{
            if(isBiometricAvailable()){
                biometricPromptAuthentication()
            }
        }
    }
    // Check if biometric hardware is available and fingerprints are enrolled
    private fun isBiometricAvailable(): Boolean {
        val biometricManager = androidx.biometric.BiometricManager.from(this)
        return biometricManager.canAuthenticate() == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
    }

    // Biometric prompt authentication logic
    @RequiresApi(Build.VERSION_CODES.P)
    private fun biometricPromptAuthentication() {
        // Create an executor to run the biometric prompt on a background thread
        val executor: Executor = ContextCompat.getMainExecutor(this)

        // Create a BiometricPrompt instance
        val biometricPrompt = androidx.biometric.BiometricPrompt(
            this@LoginScreen, // Use this@LoginScreen for the Activity context
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // If biometric authentication is successful, sign the user in automatically


                    //go and get stored email and password. then login with credentials
                    val info = getCredentials(this@LoginScreen)
                    if(info.first.isNullOrEmpty() && info.second.isNullOrEmpty())
                    {
                        Toast.makeText(applicationContext, "Please sign in first to enable biometrics", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        auth.signInWithEmailAndPassword(info.first.toString(), info.second.toString()).addOnSuccessListener{
                            Toast.makeText(applicationContext, "Authentication Successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(applicationContext, "Something went wrong. Sign in normally", Toast.LENGTH_SHORT).show()
                        }

                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication Failed. Try Again.", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Authentication Error: $errString", Toast.LENGTH_SHORT).show()
                }
            })

        // Create a BiometricPrompt info object to set title, description, etc.
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use your fingerprint to log in")
            .setDeviceCredentialAllowed(true)  // Allow device PIN, pattern, or password fallback
            .build()

        // Show the biometric prompt
        biometricPrompt.authenticate(promptInfo)
    }
    fun saveCredentials(context: Context, email: String, password: String) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        with(sharedPreferences.edit()) {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    // Function to retrieve credentials
    fun getCredentials(context: Context): Pair<String?, String?> {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)

        return Pair(email, password)
    }

}