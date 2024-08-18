package com.example.artimo_smart_frame

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide

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
        return (width * percent / 20)
    }

    fun getHeightInPercent(context: Context, percent: Int): Int {
        val height = context.resources.displayMetrics.heightPixels
        return (height* percent / 15)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val content = item as? DataTherapyModel.Result ?: return

        val art = viewHolder.view.findViewById<WebView>(R.id.art)
        val thumbnail = viewHolder.view.findViewById<ImageView>(R.id.thumbnail)
        val framebtn = viewHolder.view.findViewById<Button>(R.id.framebtn)

        content?.let {

            // 썸네일 이미지를 로드
            val thumbnailUrl = content.thumb
            Glide.with(viewHolder.view.context)
                .load(thumbnailUrl)
                .into(thumbnail)

            framebtn.setOnClickListener {
                val activity = viewHolder.view.context as? Activity
                val videoUrl = content.sources.firstOrNull()

                Log.d("ItemTherapyPresenter", "videoUrl: $videoUrl") // URL 로그 확인

                if (activity != null && !videoUrl.isNullOrEmpty()) {
                    val intent = Intent(activity, LegacyTherapyActivity::class.java).apply {
                        putExtra("videoUrl", videoUrl)
                    }
                    activity.startActivity(intent)
                } else {
                    if (activity == null) {
                        Log.e("ItemTherapyPresenter", "Activity is null")
                    }
                    if (videoUrl.isNullOrEmpty()) {
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
                    button.setTextColor(ContextCompat.getColor(button.context, R.color.brand_white)) // 원래 배경 색상으로 변경
                }
            }

            thumbnail.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    art.visibility = View.VISIBLE
                    framebtn.visibility = View.VISIBLE
                } else {
                    art.visibility = View.GONE
                    framebtn.visibility = View.GONE
                }
            }

            // 비디오 URL을 가져옴
            val videoURL = content.sources.firstOrNull()
            if (!videoURL.isNullOrEmpty()) {
                // 비디오 URL을 WebView에 로드
                art.loadUrl(videoURL)
            } else {
                Log.w("ItemTherapyPresenter", "비디오 URL이 null이거나 비어있습니다.")
            }

            thumbnail.setOnClickListener {
            }
        }

    }
    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
    }

}