package com.example.artimo_smart_frame

import android.content.Intent
import android.util.Log
import android.widget.MediaController
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.VideoView
import androidx.fragment.app.FragmentActivity
import java.io.IOException

class TherapyActivity : FragmentActivity() {
    private lateinit var gallarybtn: Button
    private lateinit var therapyArt: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapy)

        therapyArt = findViewById(R.id.therapyart)
        gallarybtn = findViewById(R.id.gallarybtn)

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
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            // 버튼의 가시성을 토글
            gallarybtn.visibility = View.VISIBLE

            // 버튼에 포커스 설정
            gallarybtn.requestFocus()

            return true // 이벤트가 처리됨
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        // 갤러리 버튼이 보이지 않을 때만 기본 뒤로 가기 동작 수행
        if (gallarybtn.visibility == View.GONE) {
            super.onBackPressed() // 기본 뒤로 가기 동작 수행
        } else {
            // 갤러리 버튼이 보이는 경우 버튼 숨기기
            gallarybtn.visibility = View.GONE
        }
    }
}
