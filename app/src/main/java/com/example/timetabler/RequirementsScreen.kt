package com.example.timetabler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.io.Serializable

class RequirementsScreen : AppCompatActivity() {

    private lateinit var textView : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requirements_screen)
        val staffSelected = intent.getSerializableExtra("staffSelected") as? ArrayList<*>
        textView = findViewById(R.id.tv)

        if (staffSelected != null) {
            for (staff in staffSelected) {
                textView.text = textView.text.toString() + staff + "\n" // Update textView content
            }
        } else {
            textView.text = "No staff selected." // Handle the null case
        }


    }
}