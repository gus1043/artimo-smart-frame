package com.example.artimo_smart_frame

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // 레이아웃 설정

        val therapybtn:ImageButton = findViewById(R.id.therapybtn)
        val defaultartbtn:ImageButton = findViewById(R.id.defaultartbtn)

        therapybtn.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()
            } else {
                v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }
        }

        defaultartbtn.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()
            } else {
                v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }
        }

        therapybtn.setOnClickListener{
            val intent = Intent(this, TherapyActivity::class.java)
            startActivity(intent)
        }

        defaultartbtn.setOnClickListener{
            val intent = Intent(this, DefaultArtActivity::class.java)
            startActivity(intent)
        }
    }

}
