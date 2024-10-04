package com.example.artimo_smart_frame

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import android.os.Handler as AndroidHandler
import java.net.URL
import java.util.concurrent.TimeUnit

class TherapyActivity : FragmentActivity() {
    private lateinit var gallarybtn: Button
    private lateinit var therapyArt: VideoView
    private lateinit var overlay: View
    private lateinit var therapyApiService: TherapyApiService
    private lateinit var handler: AndroidHandler
    private val checkInterval: Long = 3500 // 3.5초 간격

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapy)

        therapyArt = findViewById(R.id.therapyart)
        gallarybtn = findViewById(R.id.gallarybtn)
        overlay = findViewById(R.id.overlay) // 투명한 검은 배경

        // SharedPreferences에서 비디오 번호 불러옴
        val max_id = getMaxId(this)

        //일단 최대번호로 재생
        playExistingVideo(max_id)

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // 기본 URL 설정
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        therapyApiService = retrofit.create(TherapyApiService::class.java)

        handler = AndroidHandler() // Handler 초기화
        startCheckingForNewVideos(max_id) // 비디오 확인 시작

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

    private fun startCheckingForNewVideos(maxId: Int) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkForNewVideo(maxId)
                handler.postDelayed(this, checkInterval) // 5초 후에 다시 실행
            }
        }, checkInterval)
    }

    private fun checkForNewVideo(maxId: Int) {
        lifecycleScope.launch {
            try {
                // 최신 비디오 주는 api에 연결
                val dataTherapyModel = therapyApiService.getLatest()
                Log.d("TherapyActivity", "API Response: $dataTherapyModel")

                val maxIdFromJson = dataTherapyModel.result.id
                val videoUrl = dataTherapyModel.result.sources

                if (maxIdFromJson > maxId) {
                    saveMaxId(maxIdFromJson)
                    Log.d("TherapyActivity", "새로운 비디오가 있습니다: ID = $maxIdFromJson, uri = $videoUrl") // 로그 출력
                    // 비디오 다운로드 및 재생 코드 추가
                    downloadVideo(videoUrl, maxIdFromJson)

                    // 확인 주기를 일시 중지
                    handler.removeCallbacksAndMessages(null)
                    // 비디오 재생 후 3.5초 기다림
                    handler.postDelayed({
                        startCheckingForNewVideos(maxIdFromJson) // 새로운 비디오 확인 재시작
                    }, 3500) // 3.5초 후 재시작
                } else {
                    Log.d("TherapyActivity", "새로운 비디오가 없습니다: ID = $maxId") // 로그 출력
//                    playExistingVideo(maxId)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading data", e)
            }
        }
    }

    private fun playExistingVideo(maxId: Int) {
        val file = File(filesDir, "$maxId.mp4") // 파일 경로 생성

        if (file.exists()) {
            val videoUri = Uri.fromFile(file) // File 객체를 Uri로 변환
            therapyArt.setVideoURI(videoUri) // VideoView에 URI 설정

            // 비디오 준비 완료 시 자동 재생을 시작합니다.
            therapyArt.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.start()

                //IoT 제어
                CoroutineScope(Dispatchers.IO).launch {
                    // IoT control
                    processIoT(maxId)
                }

                // 비디오가 끝났을 때 반복 재생
                therapyArt.setOnCompletionListener { mediaPlayer ->
                    mediaPlayer.isLooping = true // 비디오를 부드럽게 반복 재생
                    mediaPlayer.start() // 영상이 끝나면 다시 시작
                }
            }
        } else {
            Log.w("LegacyTherapyActivity", "비디오 파일이 존재하지 않습니다: $file")
        }
    }

    private fun playNewVideo(videoId: Int) {
        val file = File(filesDir, "$videoId.mp4") // 파일 경로 생성

        if (file.exists()) {
            val videoUri = Uri.fromFile(file) // File 객체를 Uri로 변환
            therapyArt.setVideoURI(videoUri) // VideoView에 URI 설정

            // 비디오 준비 완료 시 자동 재생을 시작합니다.
            therapyArt.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true // 비디오를 부드럽게 반복 재생
                mediaPlayer.start()
                //IoT 제어
                CoroutineScope(Dispatchers.IO).launch {
                    // IoT control
                    processIoT(videoId)
                }
            }
            // 비디오가 끝났을 때 반복 재생
            therapyArt.setOnCompletionListener { mediaPlayer ->
                mediaPlayer.isLooping = true // 비디오를 부드럽게 반복 재생
                mediaPlayer.start() // 영상이 끝나면 다시 시작
            }
        } else {
            Log.w("TherapyActivity", "비디오 파일이 존재하지 않습니다: $file")
        }
    }


    private fun saveMaxId(maxId: Int) {
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("max_id", maxId)
        editor.apply()
    }

    private fun getMaxId(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("max_id", -1) // 기본값은 -1
    }

    private fun downloadVideo(videoUrl: String, id: Int) {
        // 비디오 다운로드를 비동기로 처리
        DownloadTask(this, id).execute(videoUrl)
    }

    private suspend fun processIoT(diaryId: Int) {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
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

    private class DownloadTask(val context: Context, val id: Int) : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String? {
            val videoUrl = params[0]
            return try {
                // URL 객체 생성
                val url = URL(videoUrl)
                val connection = url.openConnection()
                connection.connect()

                // 비디오 파일을 내부 저장소에 저장
                val input = BufferedInputStream(connection.getInputStream())
                val fileName = "$id.mp4"
                val fileOutput = context.openFileOutput(fileName, Context.MODE_PRIVATE)


                val data = ByteArray(1024)
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    fileOutput.write(data, 0, count)
                }

                // 파일 저장 완료 후, 저장된 파일의 경로를 로그로 출력
                val savedFile = File(context.filesDir, fileName)
                Log.d("MainActivity", "파일 저장 위치: ${savedFile.absolutePath}")
                Log.d("MainActivity", "파일 크기: ${savedFile.length()} bytes")

                // 스트림 닫기
                fileOutput.flush()
                fileOutput.close()
                input.close()


                fileName // 저장된 파일 이름 반환
            } catch (e: Exception) {
                Log.e("DownloadTask", "Error downloading video", e)
                null
            }
        }

        override fun onPostExecute(result: String?) {
            // 커스텀 토스트 레이아웃을 인플레이트
            val inflater = (context as TherapyActivity).layoutInflater
            val layout: View = inflater.inflate(R.layout.custom_toast, null)

            // 커스텀 토스트의 텍스트 설정
            val text: TextView = layout.findViewById(R.id.toast_text)
            text.text = if (result != null) {
                "새로운 테라피 아트를 가져오고 있어요!"
            } else {
                "Failed to download video with ID: $id"
            }

            // 커스텀 토스트 생성
            val toast = Toast(context)
            toast.duration = Toast.LENGTH_LONG // 기본 지속 시간 설정
            toast.view = layout

            // 토스트 위치 상단으로 설정 (100px을 30px으로 변경해 덜 내려오게)
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 30)

            // 내려오는 애니메이션 적용
            layout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down))
            toast.show()

            // 6초 동안 보여주기 위해 Handler 사용
            val handler = android.os.Handler()
            handler.postDelayed({
                layout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up)) // 올라가는 애니메이션
            }, 5500) // 6초 후 사라짐 (애니메이션이 끝나기 전 500ms 일찍 시작)
        }

    }

    override fun onBackPressed() {
        // 검은 배경과 버튼이 숨겨져 있는 상태라면 나타나게 함
        if (gallarybtn.visibility == View.GONE && overlay.visibility == View.GONE) {
            overlay.visibility = View.VISIBLE
            gallarybtn.visibility = View.VISIBLE

            // VideoView 포커스 제거 후 버튼에 포커스 설정
            therapyArt.clearFocus()
            gallarybtn.requestFocus()
        } else {
            // 검은 배경과 버튼이 보이면 숨기고 기본 뒤로 가기 동작 수행
            overlay.visibility = View.GONE
            gallarybtn.visibility = View.GONE
            super.onBackPressed()
        }
    }
}
