package com.example.artimo_smart_frame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.VideoView
import androidx.fragment.app.FragmentActivity
import java.io.File

class LegacyTherapyActivity : FragmentActivity() {
    private lateinit var gallarybtn: Button
    private lateinit var therapyArt: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legacytherapy)

        val file = intent.getStringExtra("file")
        Log.d("LegacyTherapyActivity", "file: $file")

        therapyArt = findViewById(R.id.therapyart)
        gallarybtn = findViewById(R.id.gallarybtn)

        if (!file.isNullOrEmpty()) {
            val file = File(file) // 파일 경로로 File 객체를 생성합니다.

            if (file.exists()) {
                val videoUri = Uri.fromFile(file) // File 객체를 Uri로 변환
                therapyArt.setVideoURI(videoUri) // VideoView에 URI 설정

                // 비디오 준비 완료 시 자동 재생을 시작합니다.
                therapyArt.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.start()
                }
            } else {
                Log.w("LegacyTherapyActivity", "비디오 파일이 존재하지 않습니다: $file")
            }
        } else {
            Log.e("LegacyTherapyActivity", "파일 경로가 제공되지 않았습니다.")
        }


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
            finish()
        }
    }

    override fun onBackPressed() {
        // 갤러리 버튼이 보이면 뒤로 가기
        if (gallarybtn.visibility == View.VISIBLE) {
            super.onBackPressed() // 기본 뒤로 가기 동작 수행
            finish()
        } else {
            // 갤러리 버튼이 보이지 않으면 보이게 하기
            gallarybtn.visibility = View.VISIBLE

            // 비디오뷰의 포커스를 제거
            therapyArt.clearFocus()

            // 버튼에 포커스 설정
            gallarybtn.requestFocus()
        }
    }
}
