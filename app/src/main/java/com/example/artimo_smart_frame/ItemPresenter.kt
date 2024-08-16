package com.example.artimo_smart_frame

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
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
        return (width * percent / 40)
    }

    fun getHeightInPercent(context: Context, percent: Int): Int {
        val height = context.resources.displayMetrics.heightPixels
        return (height* percent / 20)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        val content = item as? DataModel.Result
        val art = viewHolder?.view?.findViewById<ImageView>(R.id.art)

        content?.let {
            // Check if the image_path is valid and starts with the expected prefix
            val imagePath = content.image
            if (imagePath.startsWith("file:///android_asset/")) {
                val assetPath = imagePath.removePrefix("file:///android_asset/")
                val context = viewHolder?.view?.context

                try {
                    // Open the asset file and decode it to a Bitmap
                    val inputStream = context?.assets?.open(assetPath)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    art?.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                    // Handle error: you might want to set a placeholder or show an error message
                }
            } else {
                // Handle cases where the path is not valid or is not an asset
                // You might want to show a placeholder image or an error message
            }
        }
    }


    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
    }
}