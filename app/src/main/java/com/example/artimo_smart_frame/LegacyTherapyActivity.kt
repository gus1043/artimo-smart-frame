package com.example.artimo_smart_frame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class LegacyTherapyActivity : FragmentActivity() {
    private lateinit var gallarybtn: Button
    private lateinit var therapyDescriptionBtn: Button
    private lateinit var therapyArt: VideoView
    private lateinit var overlay: View
    private lateinit var infoComment: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legacytherapy)

        val file = intent.getStringExtra("file")
        val id = intent.getIntExtra("id", -1)
        Log.d("LegacyTherapyActivity", "file: $file, id: $id")

        therapyArt = findViewById(R.id.therapyart)
        gallarybtn = findViewById(R.id.gallarybtn)
        therapyDescriptionBtn = findViewById(R.id.therapyDescriptionBtn)
        overlay = findViewById(R.id.overlay) // 투명한 검은 배경

        if (id != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                fetchInfoComment(id)
            }
        }

        if (!file.isNullOrEmpty()) {
            val file = File(file) // 파일 경로로 File 객체를 생성합니다.

            if (file.exists()) {
                val videoUri = Uri.fromFile(file) // File 객체를 Uri로 변환
                therapyArt.setVideoURI(videoUri) // VideoView에 URI 설정

                // 비디오 준비 완료 시 자동 재생을 시작합니다.
                therapyArt.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.start()

                    //IoT 제어
                    CoroutineScope(Dispatchers.IO).launch {
                        // IoT control
                         processIoT(id)

                    }
                }

                // 비디오가 끝났을 때 반복 재생
                therapyArt.setOnCompletionListener { mediaPlayer ->
                    mediaPlayer.isLooping = true // 비디오를 부드럽게 반복 재생
                    mediaPlayer.start() // 영상이 끝나면 다시 시작
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

        therapyDescriptionBtn.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()
            } else {
                v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }
        }

        // '테라피 아트 설명' 버튼 클릭 리스너
        // therapyDescriptionBtn 클릭 시 infoComment를 토스트로 표시
        therapyDescriptionBtn.setOnClickListener {
            if (::infoComment.isInitialized) {
                val toast = Toast.makeText(this, infoComment, Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else {
                Toast.makeText(this, "설명 정보를 불러오고 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //IoT 제어 API 연결
    private suspend fun processIoT(diaryId: Int) {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(com.example.artimo_smart_frame.BuildConfig.BASE_URL) // 기본 URL 설정
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(TherapyApiService::class.java)

        try {
            val apiResponse = apiService.setIoT(diaryId.toString())
            if (apiResponse.isSuccessful) {
                Log.d("SetIoT", "이미지 생성 성공: ${apiResponse.body()}")
            } else {
                Log.d("SetIoT", "이미지 생성 실패: ${apiResponse.errorBody()?.string()}")
            }
        } catch (e: IOException) {
            Log.d("SetIoT", "Network error: ${e.message}")
        } catch (e: HttpException) {
            Log.d("SetIoT", "HTTP error: ${e.message}")
        } catch (e: Exception) {
            Log.d("SetIoT", "Unknown error: ${e.message}")
        }

    }

    private suspend fun fetchInfoComment(id: Int) {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(com.example.artimo_smart_frame.BuildConfig.BASE_URL) // 기본 URL 설정
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(TherapyApiService::class.java)

        try {
            // 전체 비디오 리스트를 가져옴
            val response = apiService.getVideoList()
            if (response.result.isNotEmpty()) {
                // 해당 ID와 일치하는 데이터를 필터링
                val result = response.result.find { it.id == id }

                // 해당 ID에 맞는 infoComment를 추출
                infoComment = result?.sources?.firstOrNull()?.infoComment ?: "정보 없음"
            } else {
                Log.e("LegacyTherapyActivity", "비디오 리스트가 비어 있습니다.")
            }
        } catch (e: IOException) {
            Log.e("LegacyTherapyActivity", "Network error: ${e.message}")
        } catch (e: HttpException) {
            Log.e("LegacyTherapyActivity", "HTTP error: ${e.message}")
        } catch (e: Exception) {
            Log.e("LegacyTherapyActivity", "Unknown error: ${e.message}")
        }
    }

    override fun onBackPressed() {
        // 검은 배경과 버튼이 숨겨져 있는 상태라면 나타나게 함
        if (gallarybtn.visibility == View.GONE && overlay.visibility == View.GONE) {
            overlay.visibility = View.VISIBLE
            gallarybtn.visibility = View.VISIBLE
            therapyDescriptionBtn.visibility = View.VISIBLE

            // VideoView 포커스 제거 후 버튼에 포커스 설정
            therapyArt.clearFocus()
            gallarybtn.requestFocus()
        } else {
            // 검은 배경과 버튼이 보이면 숨기고 기본 뒤로 가기 동작 수행
            overlay.visibility = View.GONE
            gallarybtn.visibility = View.GONE
            therapyDescriptionBtn.visibility = View.GONE
            super.onBackPressed()
        }
    }

}
