package com.example.artimo_smart_frame

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class DefaultArtActivity: FragmentActivity() {
    lateinit var defaultartFragment: DefaultArtFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_defaultart)

        defaultartFragment = DefaultArtFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.defaultart_fragment, defaultartFragment)
        transaction.commit()

        val gson = Gson()
        val inputStream: InputStream = assets.open("art.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val dataList: DataModel = gson.fromJson(bufferedReader, DataModel::class.java)

        defaultartFragment.bindData(dataList)
    }
}
