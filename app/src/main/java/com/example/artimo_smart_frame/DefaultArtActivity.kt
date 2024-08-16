package com.example.artimo_smart_frame

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class DefaultArtActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_defaultart) // 레이아웃 설정
    }
}