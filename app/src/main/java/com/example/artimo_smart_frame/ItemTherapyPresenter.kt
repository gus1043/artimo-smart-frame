package com.example.artimo_smart_frame

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.VideoView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class ItemTherapyPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val view = LayoutInflater.from(requireNotNull(parent).context).inflate(R.layout.item_view_therapy, parent, false)

        val params = view.layoutParams
        params.width = getWidthInPercent(parent.context, 10)
        params.height = getHeightInPercent(parent.context, 10)
        return ViewHolder(view)
    }

    fun getWidthInPercent(context: Context, percent: Int): Int {
        val width = context.resources.displayMetrics.widthPixels
        return (width * percent / 22)
    }

    fun getHeightInPercent(context: Context, percent: Int): Int {
        val height = context.resources.displayMetrics.heightPixels
        return (height* percent / 17)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val content = item as? DataTherapyModel.Result ?: return

        val art = viewHolder.view.findViewById<VideoView>(R.id.art)
        val thumbnail = viewHolder.view.findViewById<ImageView>(R.id.thumbnail)
        val framebtn = viewHolder.view.findViewById<Button>(R.id.framebtn)

        content?.let {

            // 썸네일 이미지를 로드
            val thumbnailUrl = content.thumb
            Glide.with(viewHolder.view.context)
                .load(thumbnailUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(thumbnail)

            thumbnail.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    art.visibility = View.VISIBLE
                    framebtn.visibility = View.VISIBLE
                } else {
                    art.visibility = View.GONE
                    framebtn.visibility = View.GONE
                }
            }

            //버튼 텍스트에 날짜 추가
            val formattedDate = formatDate(content.createdAt)
            framebtn.text = "$formattedDate 미디어 아트를 스마트 액자에 적용"

            // 비디오 URL을 가져옴
            val context = viewHolder.view.context
            val file = File(context.filesDir, "${content.id}.mp4") // 내부 저장소에서 비디오 파일 찾기
            Log.d("ItemTherapyPresenter", "비디오 파일 : $file")
            if (file.exists()) {
                val videoUri = Uri.fromFile(file) // File 객체를 Uri로 변환
                art.setVideoURI(videoUri) // VideoView에 URI 설정

                // 비디오 준비 완료 시 자동 재생을 시작합니다.
                art.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.start()
                }
            } else {
                Log.w("ItemTherapyPresenter", "비디오 파일이 존재하지 않습니다: $file")
            }


            thumbnail.setOnClickListener {
            }

            framebtn.setOnClickListener {
                val activity = viewHolder.view.context as? Activity
                Log.d("ItemTherapyPresenter", "videoUrl: $file") // URL 로그 확인

                if (activity != null && file.exists()) {
                    //IoT 제어
                    CoroutineScope(Dispatchers.IO).launch {
                        // IoT control
                        processIoT(content.id)

                        // After completing the IoT process, switch back to the main thread to start the new activity
                        withContext(Dispatchers.Main) {
                            val intent = Intent(activity, LegacyTherapyActivity::class.java).apply {
                                putExtra("file", file.toString())
                            }
                            activity.startActivity(intent)
                            activity.finish()
                        }
                    }
                } else {
                    if (activity == null) {
                        Log.e("ItemTherapyPresenter", "Activity is null")
                    }
                    if (!file.exists()) {
                        Log.e("ItemTherapyPresenter", "Video URL is null or empty")
                    }
                }
            }

            framebtn.setOnFocusChangeListener { v, hasFocus ->
                val button = v as Button
                if (hasFocus) {
                    // 포커스가 있을 때 framebtn의 배경 변경
                    button.setTextColor(Color.parseColor("#FFFF00"))
                } else {
                    // 포커스가 없을 때 기본 배경으로 변경
                    button.setTextColor(
                        ContextCompat.getColor(
                            button.context,
                                R.color.brand_white
                        )
                    )
                }
            }
        }
    }
    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
    }

    fun formatDate(inputDate: String): String {
        // 입력 형식에 맞는 SimpleDateFormat 정의
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        // 출력 형식 정의
        val outputFormat = SimpleDateFormat("M/d", Locale.getDefault())

        // 문자열을 Date 객체로 변환
        val date = inputFormat.parse(inputDate)
        // 원하는 형식으로 출력
        return outputFormat.format(date)
    }

    //IoT 제어 API 연결
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

}