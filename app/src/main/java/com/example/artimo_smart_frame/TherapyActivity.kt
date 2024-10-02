package com.example.artimo_smart_frame

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
    private lateinit var therapyApiService: TherapyApiService
    private lateinit var handler: AndroidHandler
    private val checkInterval: Long = 5000 // 5초 간격

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapy)

        therapyArt = findViewById(R.id.therapyart)
        gallarybtn = findViewById(R.id.gallarybtn)

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
                    // 비디오 재생 후 5초 기다림
                    handler.postDelayed({
                        startCheckingForNewVideos(maxIdFromJson) // 새로운 비디오 확인 재시작
                    }, 5000) // 5초 후 재시작
                } else {
                    Log.d("TherapyActivity", "새로운 비디오가 없습니다: ID = $maxId") // 로그 출력
                    playExistingVideo(maxId)
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
            // 다운로드 완료 메시지 표시
            if (result != null) {
                Toast.makeText(context, "새 영상을 다운로드 중입니다!: $id", Toast.LENGTH_SHORT).show() // Toast 메시지로 변경
                Log.d("TherapyActivity", "Downloaded video saved as: $result")

                (context as TherapyActivity).playNewVideo(id) // 새 비디오 재생 호출
            } else {
                Toast.makeText(context, "Failed to download video with ID: $id", Toast.LENGTH_SHORT).show()
            }
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

            // 웹뷰의 포커스를 제거
            therapyArt.clearFocus()

            // 버튼에 포커스 설정
            gallarybtn.requestFocus()
        }
    }
}
