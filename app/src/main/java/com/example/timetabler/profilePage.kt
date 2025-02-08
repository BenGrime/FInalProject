package com.example.timetabler

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.EmailAuthProvider

class profilePage : AppCompatActivity() {

    private lateinit var logoutBtn : LinearLayout
    private lateinit var changeEmail : LinearLayout
    private lateinit var changePassword : LinearLayout
    private lateinit var auth: FirebaseAuth;
    private lateinit var dialog : Dialog
    private lateinit var cancel : MaterialButton
    private lateinit var confirm : MaterialButton
    private lateinit var inputPassword : TextInputEditText
    private lateinit var newEmailInput : TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) 
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        auth = Firebase.auth
        logoutBtn = findViewById(R.id.logout)
        logoutBtn.setOnClickListener{
            auth.signOut()
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
            finish()
        }
//        changeEmail = findViewById(R.id.changeEmail)
//        changeEmail.setOnClickListener{
//            dialog = Dialog(this)
//            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
//            dialog.setCancelable(true)
//            dialog.setContentView(R.layout.change_email)
//
//            dialog.show()
//
//            cancel = dialog.findViewById(R.id.changeEmailCancel)
//            cancel.setOnClickListener{
//                dialog.dismiss()
//            }
//            confirm = dialog.findViewById(R.id.changeEmailConfirm)
//            confirm.setOnClickListener {
//
//                newEmailInput = dialog.findViewById(R.id.newEmail)
//                inputPassword = dialog.findViewById(R.id.oldPassword)
//
//                val user = FirebaseAuth.getInstance().currentUser
//                val currentEmail = user?.email
//                var password = inputPassword.text.toString()
//                var newEmail = newEmailInput.text.toString()
//                // Step 1: Reauthenticate the user
//                if (password.isNotEmpty() && newEmail.isNotEmpty()) {
//                    val credential = currentEmail?.let { EmailAuthProvider.getCredential(it, password) }
//
//                    // Step 1: Reauthenticate the user
//                    if (credential != null) {
//                        user?.reauthenticate(credential)?.addOnCompleteListener { authTask ->
//                            if (authTask.isSuccessful) {
//                                // Step 2: Update the email
//                                user.updateEmail(newEmail).addOnCompleteListener { updateTask ->
//                                    if (updateTask.isSuccessful) {
//                                        Toast.makeText(this, "Email updated successfully!", Toast.LENGTH_SHORT).show()
//                                        dialog.dismiss()
//                                    } else {
//                                        Toast.makeText(this, "Failed to update email: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
//                                        println(updateTask.exception?.message)
//                                    }
//                                }
//                            } else {
//                                Toast.makeText(this, "Re-authentication failed. Incorrect password?", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                } else {
//                    Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//        }
        changePassword = findViewById(R.id.changePassword)
        changePassword.setOnClickListener{
            auth.sendPasswordResetEmail(auth.currentUser?.email.toString()).addOnSuccessListener{
                Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show()
            }
        }
    }
}