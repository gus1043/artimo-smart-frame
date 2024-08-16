package com.example.artimo_smart_frame

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // 레이아웃 설정

        val therapybtn:ImageButton = findViewById(R.id.therapybtn)
        therapybtn.setOnClickListener{
            val intent = Intent(this, TherapyActivity::class.java)
            startActivity(intent)
        }

        val defaultartbtn:ImageButton = findViewById(R.id.defaultartbtn)
        defaultartbtn.setOnClickListener{
            val intent = Intent(this, DefaultArtActivity::class.java)
            startActivity(intent)
        }
    }

}
