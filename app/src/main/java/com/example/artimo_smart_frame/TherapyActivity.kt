package com.example.artimo_smart_frame

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.VideoView
import androidx.fragment.app.FragmentActivity

class TherapyActivity : FragmentActivity() {
    private lateinit var gallarybtn: Button
    private lateinit var therapyArt: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapy)

        therapyArt = findViewById(R.id.therapyart)
        gallarybtn = findViewById(R.id.gallarybtn)
        val videoURL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        therapyArt.loadUrl(videoURL)


        gallarybtn.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()
            } else {
                v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }
        }

        gallarybtn.setOnClickListener {
            val intent = Intent(this, TherapyGalleryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        // 갤러리 버튼이 보이면 뒤로 가기
        if (gallarybtn.visibility == View.VISIBLE) {
            super.onBackPressed() // 기본 뒤로 가기 동작 수행
        } else {
            // 갤러리 버튼이 보이지 않으면 보이게 하기
            gallarybtn.visibility = View.VISIBLE
            // 웹뷰의 포커스를 제거
            therapyArt.clearFocus()
            // 웹뷰의 포커스를 제거
            therapyArt.clearFocus()

            // 버튼에 포커스 설정
            gallarybtn.requestFocus()
        }
    }
}
