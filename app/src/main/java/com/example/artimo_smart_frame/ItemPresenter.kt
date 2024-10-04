package com.example.artimo_smart_frame

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.Presenter
import java.io.IOException

class ItemPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_view, parent, false)

        val params = view.layoutParams
        params.width=getWidthInPercent(parent!!.context, 10)
        params.height=getHeightInPercent(parent!!.context , 10)
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


    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        val content = item as? DataModel.Result
        val art = viewHolder?.view?.findViewById<ImageView>(R.id.art)
        val overlayText = viewHolder?.view?.findViewById<View>(R.id.overlayText)
        val textView = viewHolder?.view?.findViewById<TextView>(R.id.title)

        content?.let {
            val imagePath = content.image
            val imageType = content.type
            val title = content.title
            val painter = content.painter
            val hue = content.hue
            val saturation = content.saturation

            // title과 painter를 콤마로 연결
            val combinedText = "$title \n$painter"
            textView?.text = combinedText // TextView에 설정

            if (imagePath.startsWith("file:///android_asset/")) {
                val assetPath = imagePath.removePrefix("file:///android_asset/")
                val context = viewHolder?.view?.context

                try {
                    // asset 파일 열고 Bitmap으로 디코딩
                    val inputStream = context?.assets?.open(assetPath)
                    val drawable = Drawable.createFromStream(inputStream, null)
                    art?.setImageDrawable(drawable)

                    // 포커스 변경 리스너 설정
                    art?.setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            overlayText?.visibility = View.VISIBLE
                            textView?.visibility = View.VISIBLE
                        } else {
                            overlayText?.visibility = View.GONE
                            textView?.visibility = View.GONE
                        }
                    }

                    // 이미지를 클릭할 때의 동작 설정
                    art?.setOnClickListener {
                        val intent = Intent(context, ArtframeActivity::class.java)

                        // 이미지 파일 이름을 전달
                        intent.putExtra("art", assetPath)
                        intent.putExtra("type", imageType.toString())
                        intent.putExtra("hue", hue.toString())
                        intent.putExtra("saturation", saturation.toString())

                        context?.startActivity(intent)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
    }
}