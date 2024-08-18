package com.example.artimo_smart_frame

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class TherapyGalleryActivity : FragmentActivity() {
    lateinit var therapyArtFragment: TherapyArtFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_therapygallary)

        therapyArtFragment = TherapyArtFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.therapyart_fragment, therapyArtFragment)
        transaction.commit()

        try {
            val gson = Gson()
            val inputStream: InputStream = assets.open("therapy.json")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val dataList: DataTherapyModel = gson.fromJson(bufferedReader, DataTherapyModel::class.java)


            Log.d("TherapyGalleryActivity", "전달 받은 데이터: ${dataList.result}")

            therapyArtFragment.bindData(dataList)
        } catch (e: Exception) {
            Log.e("TherapyGalleryActivity", "Error reading JSON", e)
        }
    }
}
