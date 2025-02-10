package com.example.timetabler

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.print.PrintAttributes.Margins
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
import androidx.core.view.marginRight
import androidx.core.view.size
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
                                    recreate()
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
        save.setOnClickListener{
            val managerPairs = ArrayList<Pair<String, Int>>()

            for (i in 0 until managersList.childCount) {
                val managerLinearLayout = managersList.getChildAt(i) as? LinearLayout
                managerLinearLayout?.let {
                    val mainRow = it.getChildAt(0) as? LinearLayout // Assuming mainRow is the first child
                    mainRow?.let { row ->
                        val managerTextView = row.getChildAt(0) as? TextView // Assuming it's the first child
                        managerTextView?.let { textView ->
                            val name = textView.text.toString() // Get manager name

                            // Find the dropdownSection (second child of managerLinearLayout)
                            val dropdownSection = managerLinearLayout.getChildAt(1) as? LinearLayout
                            dropdownSection?.let { dropdown ->
                                // Get the inner LinearLayout (first child of dropdownSection)
                                val innerLayout = dropdown.getChildAt(1) as? LinearLayout
                                innerLayout?.let { inner ->
                                    // Loop through innerLayout's children to find the Spinner
                                    for (j in 0 until inner.childCount) {
                                        val child = inner.getChildAt(j)
                                        if (child is Spinner && child.tag == name) {
                                            val selectedValue = child.selectedItem as? Int ?: 0 // Get selected value
                                            managerPairs.add(Pair(name, selectedValue)) // Store in list
                                            break // Stop searching once found
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for(pair in managerPairs) {
                db.collection("Managers").whereEqualTo("name", pair.first).get().addOnSuccessListener{
                    val updateMap = mapOf("accessLevel" to pair.second)
                    db.collection("Managers").document(it.documents.first().id).update(updateMap).addOnSuccessListener{
                        Toast.makeText(this, "UPDATED", Toast.LENGTH_SHORT).show()
                    }
                }

            }


        }
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
                            backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
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
                            setTypeface(typeface, Typeface.BOLD)
                            setTextColor(ContextCompat.getColor(context, R.color.black))
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
                            setBackgroundColor(ContextCompat.getColor(context, R.color.white)) // Different color for dropdown

                            val dividerView = View(this@ManageUsers).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, // Match parent width
                                    2 // Height of 1dp
                                ).apply {
                                    setMargins(0, 16, 0, 16) // Vertical margin of 16dp
                                }
                                setBackgroundColor(ContextCompat.getColor(context, R.color.black)) // Set background color
                            }

                            val rowLayout = LinearLayout(this@ManageUsers).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                orientation = LinearLayout.HORIZONTAL
                                gravity = Gravity.CENTER_VERTICAL // Align items in the center vertically
                            }

                            val textView = TextView(this@ManageUsers).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1.5f // More weight to push Spinner slightly right
                                )
                                text = "Change Access Level"
                            }

                            val spinner = Spinner(this@ManageUsers).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f // Slightly to the right, but not centered
                                ).apply {
                                    marginEnd = 20
                                }
                            }
                            spinner.tag = m.name

                            auth.currentUser?.let { it1 ->
                                fh.getManager(it1.uid){
                                    if(it.accessLevel == 1)
                                    {
                                        spinner.adapter = ArrayAdapter(this@ManageUsers, android.R.layout.simple_spinner_dropdown_item, listOf(2,3,4))
                                    }
                                    else
                                    {
                                        spinner.adapter = ArrayAdapter(this@ManageUsers, android.R.layout.simple_spinner_dropdown_item, listOf(3,4))
                                    }

                                }
                            }

                            val deleteIcon = ImageView(this@ManageUsers).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    100, 100
                                ).apply {
                                    marginStart = 32 // Extra spacing from Spinner
                                }
                                setImageResource(R.drawable.bin_icon)
                                setColorFilter(ContextCompat.getColor(context, R.color.red))
                            }

                            deleteIcon.setOnClickListener{
                                db.collection("Managers").get().addOnSuccessListener{
                                    for(document in it){
                                        if(document.get("name") == managerTextView.text.toString()){
                                            db.collection("Managers").document(document.id).delete().addOnSuccessListener{
                                                Toast.makeText(this@ManageUsers, managerTextView.text.toString() + " successfully deleted!", Toast.LENGTH_SHORT).show()
                                                recreate()
                                            }
                                        }
                                    }
                                }
                            }

                            // Add views to row layout
                            rowLayout.addView(textView)  // Left-aligned text
                            rowLayout.addView(spinner)   // Slightly to the right
                            rowLayout.addView(deleteIcon) // Right-aligned icon
                            addView(dividerView)
                            addView(rowLayout)
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
//        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        view.measure(
            View.MeasureSpec.makeMeasureSpec((view.parent as View).width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
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