package com.example.artimo_smart_frame

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import java.io.IOException

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
        return (width * percent / 25)
    }

    fun getHeightInPercent(context: Context, percent: Int): Int {
        val height = context.resources.displayMetrics.heightPixels
        return (height* percent / 15)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val content = item as? DataTherapyModel.Result ?: return

        val art = viewHolder.view.findViewById<WebView>(R.id.art)
        val thumbnail = viewHolder.view.findViewById<ImageView>(R.id.thumbnail)

        // 썸네일 이미지를 로드
        val thumbnailUrl = content.thumb
        Glide.with(viewHolder.view.context)
            .load(thumbnailUrl)
            .into(thumbnail)

        thumbnail.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                art.visibility = View.VISIBLE
            }else{
                art.visibility = View.GONE
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
    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
    }
}