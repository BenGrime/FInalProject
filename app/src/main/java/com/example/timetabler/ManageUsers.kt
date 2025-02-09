package com.example.timetabler

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ManageUsers : AppCompatActivity()
{
    private lateinit var managersList : GridLayout
    private lateinit var save : LinearLayout
    private lateinit var backBtn : ImageView
    private lateinit var create : ImageView
    private lateinit var dialog : Dialog

    private lateinit var emailBox : TextInputEditText
    private lateinit var nameBox : TextInputEditText
    private lateinit var levelSelect : Spinner
    private lateinit var cancel : MaterialButton
    private lateinit var confirm : MaterialButton

    private val db = Firebase.firestore
    var fh = FirebaseHandler()
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        auth = Firebase.auth
        create = findViewById(R.id.addManager)
        create.setOnClickListener{
            dialog = Dialog(this)
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.create_manager)
            emailBox = dialog.findViewById(R.id.emailInput)
            nameBox = dialog.findViewById(R.id.nameInput)
            levelSelect = dialog.findViewById(R.id.acessLevelSelect)
            auth.currentUser?.let { it1 ->
                fh.getManager(it1.uid){
                    if(it.accessLevel == 1)
                    {
                        levelSelect.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf(2,3,4))
                    }
                    else
                    {
                        levelSelect.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf(3,4))
                    }

                }
            }

            cancel = dialog.findViewById(R.id.createManagerCancel)
            confirm = dialog.findViewById(R.id.createManagerConfirm)
            cancel.setOnClickListener{
                dialog.dismiss()
            }
            confirm.setOnClickListener{
                if(emailBox.text.toString().isNotEmpty() && nameBox.text.toString().isNotEmpty())
                {
                    val m = Manager(accessLevel = levelSelect.selectedItem as Int, nameBox.text.toString())
                    auth.createUserWithEmailAndPassword(emailBox.text.toString(), "Password").addOnSuccessListener{
                        db.collection("Managers").document(it.user!!.uid).set(m).addOnSuccessListener{
                            auth.sendPasswordResetEmail(emailBox.text.toString()).addOnSuccessListener{
                                auth.signOut()
                                var credentials = getCredentials(this)
                                auth.signInWithEmailAndPassword(credentials.first.toString(), credentials.second.toString()).addOnSuccessListener{
                                    Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                            }.addOnFailureListener{
                                Toast.makeText(this, "Couldn't send emil", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener{
                            Toast.makeText(this, "Something Went Wrong, contact IT", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener{
                        Toast.makeText(this, "Couldn't Create Account", Toast.LENGTH_SHORT).show()
                    }
                }
                else
                {
                    Toast.makeText(this, "Please fill in fields", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
        }
        backBtn = findViewById(R.id.backBtnManage)
        backBtn.setOnClickListener{
            finish()
        }

        managersList = findViewById(R.id.managersList)
        save = findViewById(R.id.saveChanges)
        var accessLevelCurrent = 0
        fh.getManagers{
            fh.getManager(auth.currentUser!!.uid) {manager ->
                for (m in it)
                {
                    if(manager.name == m.name){ accessLevelCurrent = manager.accessLevel }
                }
                for(m in it)
                {
                    if(m.accessLevel > accessLevelCurrent)
                    {
                        val managerLinearLayout = LinearLayout(this@ManageUsers).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(8, 30, 8, 30)
                            }
                            orientation = LinearLayout.VERTICAL // Change to vertical to allow dropdown
                            setPadding(16, 30, 16, 30)
                            setBackgroundResource(R.drawable.rounded_rectangle)
                            backgroundTintList =
                                ContextCompat.getColorStateList(context, R.color.customGreen)
                        }

                        val mainRow = LinearLayout(this@ManageUsers).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                        }

                        val managerTextView = TextView(this@ManageUsers).apply {
                            text = m.name
                            textSize = 20f
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                            setPadding(8, 8, 8, 8)
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }

                        val imageView = ImageView(this@ManageUsers).apply {
                            layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                                marginEnd = 10
                            }
                            setImageResource(R.drawable.arrow2)
                            setColorFilter(ContextCompat.getColor(context, R.color.gray))
                            (layoutParams as LinearLayout.LayoutParams).gravity = Gravity.CENTER
                        }

                        // Expandable Content Section (Initially Hidden)
                        val dropdownSection = LinearLayout(this@ManageUsers).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            orientation = LinearLayout.VERTICAL
                            visibility = View.GONE // Hidden initially
                            setPadding(16, 8, 16, 8)
                            setBackgroundColor(ContextCompat.getColor(context, R.color.lightBlue)) // Different color for dropdown

                            val actionTextView = TextView(this@ManageUsers).apply {
                                text = "Manage this Manager"
                                textSize = 16f
                                setTextColor(ContextCompat.getColor(context, R.color.white))
                                setPadding(8, 8, 8, 8)
                            }

                            addView(actionTextView) // Add extra options here
                        }

                        // Add click listener to expand/collapse the dropdown
                        managerLinearLayout.setOnClickListener {
                            if (dropdownSection.visibility == View.GONE) {
                                expandView(dropdownSection) // Expands with animation
                                imageView.animate().rotation(90f).setDuration(300)
                                    .start() // Rotate down
                            } else {
                                collapseView(dropdownSection) // Collapses with animation
                                imageView.animate().rotation(0f).setDuration(300)
                                    .start() // Rotate back up
                            }
                        }

                        mainRow.addView(managerTextView)
                        mainRow.addView(imageView)

                        managerLinearLayout.addView(mainRow)
                        managerLinearLayout.addView(dropdownSection) // Add hidden section

                        managersList.addView(managerLinearLayout)
                    }
                }

            }

        }

    }

    private fun expandView(view: View) {
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val targetHeight = view.measuredHeight

        view.layoutParams.height = 0
        view.visibility = View.VISIBLE

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                view.layoutParams.height = if (interpolatedTime == 1f) {
                    LinearLayout.LayoutParams.WRAP_CONTENT
                } else {
                    (targetHeight * interpolatedTime).toInt()
                }
                view.requestLayout()
            }
        }
        animation.duration = 300 // Animation time in ms
        view.startAnimation(animation)
    }

    private fun collapseView(view: View) {
        val initialHeight = view.measuredHeight

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                if (interpolatedTime == 1f) {
                    view.visibility = View.GONE
                } else {
                    view.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    view.requestLayout()
                }
            }
        }
        animation.duration = 300
        view.startAnimation(animation)
    }

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