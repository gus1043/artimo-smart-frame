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
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

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

        val frameLayout = viewHolder.view.findViewById<FrameLayout>(R.id.frameLayout)
        val art = viewHolder.view.findViewById<VideoView>(R.id.art)
        val thumbnail = viewHolder.view.findViewById<ImageView>(R.id.thumbnail)
        val overlay = viewHolder.view.findViewById<View>(R.id.overlay)
        val overlayText = viewHolder.view.findViewById<TextView>(R.id.overlayText)

        val context = viewHolder.view.context

        // Activity가 파괴되지 않았는지 확인
        if (context is Activity && !context.isDestroyed && !context.isFinishing) {
            // 썸네일 이미지를 로드
            val thumbnailUrl = content.thumb
            Glide.with(context)
                .load(thumbnailUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(thumbnail)

            // FrameLayout에 포커스 이벤트 처리
            frameLayout.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    thumbnail.setImageDrawable(null)
                    art.visibility = View.VISIBLE
                    overlay.visibility = View.VISIBLE
                    overlayText.visibility = View.VISIBLE

                    // 확대 애니메이션 (팝업 효과)
                    frameLayout.animate()
                        .scaleX(1.1f) // 약간 확대
                        .scaleY(1.1f)
                        .setDuration(300) // 애니메이션 지속 시간
                        .start()

                } else {

                    // 축소 애니메이션 (원래 크기로 복원)
                    frameLayout.animate()
                        .scaleX(1.0f) // 원래 크기로 복원
                        .scaleY(1.0f)
                        .setDuration(300)
                        .start()

                    // 포커스를 잃었을 때 VideoView를 중지하고 숨김
                    art.stopPlayback() // 비디오 재생 중지
                    art.visibility = View.GONE

                    // 썸네일 이미지 다시 로드
                    if (context is Activity && !context.isDestroyed && !context.isFinishing) {
                        Glide.with(thumbnail.context)
                            .load(content.thumb)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(thumbnail)

                        // 오버레이와 텍스트 숨김
                        overlay.visibility = View.GONE
                        overlayText.visibility = View.GONE
                    }
                }
            }

            //텍스트에 날짜
            val formattedDate = formatDate(content.createdAt)
            overlayText.text = "$formattedDate 의 테라피 아트"

            // 비디오 URL을 가져옴
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


            // 클릭 이벤트 처리
            frameLayout.setOnClickListener {
                val activity = viewHolder.view.context as? Activity
                Log.d("ItemTherapyPresenter", "videoUrl: $file") // URL 로그 확인

                if (activity != null && file.exists()) {
                    val intent = Intent(activity, LegacyTherapyActivity::class.java).apply {
                        putExtra("file", file.toString())
                        putExtra("id", content.id)
                    }
                    activity.startActivity(intent)
                    activity.finish()
                } else {
                    if (activity == null) {
                        Log.e("ItemTherapyPresenter", "Activity is null")
                    }
                    if (!file.exists()) {
                        Log.e("ItemTherapyPresenter", "Video file does not exist: $file")
                    }
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
        val outputFormat = SimpleDateFormat("M월 d일", Locale.getDefault())

        // 문자열을 Date 객체로 변환
        val date = inputFormat.parse(inputDate)
        // 원하는 형식으로 출력
        return outputFormat.format(date)
    }

}