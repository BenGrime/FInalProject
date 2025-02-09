package com.example.timetabler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.mifmif.common.regex.Main

class settings_selection_page : AppCompatActivity() {
    private lateinit var profileBtn : LinearLayout
    private lateinit var algorDepBtn : LinearLayout
    private lateinit var databaseBtn : LinearLayout
    private lateinit var manageUsersBtn : LinearLayout
    private lateinit var backBtn : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_selection_page)
        var accessLevel = intent.extras?.get("accessLevel") as? Int ?: -1
        backBtn = findViewById(R.id.backBtnSettingsSelect)
        backBtn.setOnClickListener{
            finish()
        }

        profileBtn = findViewById(R.id.profileBtn)
        profileBtn.setOnClickListener{
            val intent = Intent(this, profilePage::class.java)
            startActivity(intent)
        }
        algorDepBtn = findViewById(R.id.algorithmDepenBtn)
        algorDepBtn.setOnClickListener{
            val intent = Intent(this, Settings_Page::class.java)
            startActivity(intent)
        }
        databaseBtn = findViewById(R.id.databaseBtn)
        databaseBtn.setOnClickListener{
            val intent = Intent(this, Settings_Page::class.java)
            startActivity(intent)
        }
        manageUsersBtn = findViewById(R.id.manageUsersBtn)
        manageUsersBtn.setOnClickListener{
            val intent = Intent(this, ManageUsers::class.java)
            startActivity(intent)
        }
        if(accessLevel >= 3)
        {
            algorDepBtn.visibility = View.GONE
            databaseBtn.visibility = View.GONE
            manageUsersBtn.visibility = View.GONE
        }



    }
}