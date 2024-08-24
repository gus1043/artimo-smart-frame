package com.example.artimo_smart_frame

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.net.URL

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val therapybtn: ImageButton = findViewById(R.id.therapybtn)
        val defaultartbtn: ImageButton = findViewById(R.id.defaultartbtn)

        // 버튼에 포커스 애니메이션 설정
        setButtonFocusAnimation(therapybtn)
        setButtonFocusAnimation(defaultartbtn)

        // 버튼 클릭 리스너 설정
        therapybtn.setOnClickListener {
            startActivity(Intent(this, TherapyActivity::class.java))
        }

        defaultartbtn.setOnClickListener {
            startActivity(Intent(this, DefaultArtActivity::class.java))
        }

        // 메인에서 데이터 받아오기
        loadDataFromAssets()
    }

    // 버튼 포커스 시 버튼 크기 확장
    private fun setButtonFocusAnimation(button: ImageButton) {
        button.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start()
            } else {
                v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }
        }
    }

    // 현재 json의 최대 id를 sharedpreference에 저장함
    private fun saveMaxId(context: Context, maxId: Int) {
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("max_id", maxId)
        editor.apply()
    }

    private fun getMaxId(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("max_id", -1) // 기본값은 -1
    }

    //sharedpreference에 저장된 값과 현재의 값 중 더 큰 값을 비교해서 data 받아옴
    private fun loadDataFromAssets() {
        try {
            val gson = Gson()
            val inputStream: InputStream = assets.open("therapy.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val dataTherapyModel = gson.fromJson(jsonString, DataTherapyModel::class.java)

            // SharedPreferences에서 max_id를 불러옴
            val maxIdFromPrefs = getMaxId(this)

            // JSON에서 max_id
            val maxIdFromJson = dataTherapyModel.result.maxOfOrNull { it.id } ?: -1

            // maxIdFromJson 이 prefence꺼 보다 큰 경우
            if (maxIdFromJson > maxIdFromPrefs) {
                saveMaxId(this, maxIdFromJson)  //max_id 업데이트

                // 각 Result 객체에 대해 비디오를 다운로드하고 저장
                dataTherapyModel.result.forEach { result ->
                    if (result.id > maxIdFromPrefs) { // SharedPreferences의 max_id보다 큰 경우
                        val videoUrl = result.sources.firstOrNull()
                        videoUrl?.let {
                            downloadVideo(it, result.id) // result.id 사용
                        }
                    }
                }
            } else {
                Log.d("MainActivity", "새 비디오 없음.")
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading JSON", e)
        }
    }


    private fun downloadVideo(videoUrl: String, id: Int) {
        // 비디오 다운로드를 비동기로 처리
        DownloadTask(this, id).execute(videoUrl)
    }

    private class DownloadTask(val context: Context, val id: Int) : AsyncTask<String, Void, String>() {
        private lateinit var progressDialog: ProgressDialog
        // progresdialog로 다운로드 시각화
        override fun onPreExecute() {
            super.onPreExecute()
            // ProgressDialog 초기화 및 설정
            progressDialog = ProgressDialog(context , R.style.CustomProgressDialog)
            progressDialog.setMessage("감정 테라피 아트 다운로드 중...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg params: String?): String? {
            val videoUrl = params[0]
            return try {
                val url = URL(videoUrl)
                val connection = url.openConnection()
                connection.connect()

                val input = BufferedInputStream(connection.getInputStream())
                val fileName = "$id.mp4"
                val fileOutput = context.openFileOutput(fileName, Context.MODE_PRIVATE)

                val data = ByteArray(1024)
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    fileOutput.write(data, 0, count)
                }

                val savedFile = File(context.filesDir, fileName)
                Log.d("MainActivity", "파일 저장 위치: ${savedFile.absolutePath}")
                Log.d("MainActivity", "파일 크기: ${savedFile.length()} bytes")

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
            // 다운로드 완료 시 ProgressDialog 닫기
            progressDialog.dismiss()

            if (result != null) {
//                Toast.makeText(context, "다운로드에 완료했습니다.", Toast.LENGTH_SHORT).show()
                Log.d("MainActivity", "Downloaded video saved as: $result")
            } else {
                Toast.makeText(context, "다운로드 실패했습니다. ID: $id", Toast.LENGTH_SHORT).show()
            }
        }
    }
}