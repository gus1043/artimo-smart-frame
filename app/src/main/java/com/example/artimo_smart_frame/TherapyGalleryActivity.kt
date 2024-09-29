package com.example.artimo_smart_frame

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class TherapyGalleryActivity : FragmentActivity() {
    lateinit var therapyArtFragment: TherapyArtFragment
    private lateinit var therapyApiService: TherapyApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapygallary)

        therapyArtFragment = TherapyArtFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.therapyart_fragment, therapyArtFragment)
        transaction.commit()

        try {

            // Retrofit 초기화
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL) // 기본 URL 설정
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            therapyApiService = retrofit.create(TherapyApiService::class.java)

            lifecycleScope.launch {
                try {
                    val dataTherapyModel = therapyApiService.getVideoList()
                    Log.d("TherapyGalleryActivity", "전달 받은 데이터: ${dataTherapyModel.result}")

                    therapyArtFragment.bindData(dataTherapyModel)

                } catch (e: Exception) {
                    Toast.makeText(this@TherapyGalleryActivity, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Error loading data", e)
                }
            }
        } catch (e: Exception) {
            Log.e("TherapyGalleryActivity", "Error reading JSON", e)
        }
    }
}
