package com.example.artimo_smart_frame

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import java.io.IOException

class ArtframeActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artframe) // Set the layout

        // Retrieve image file name and type from Intent
        val imageFileName = intent.getStringExtra("art")
        val imageType = intent.getStringExtra("type")?.toIntOrNull()

        // Get the ImageView
        val imageView: ImageView = findViewById(R.id.artframe)

        try {
            // Use AssetManager to read the image file
            val inputStream = assets.open(imageFileName ?: "")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Rotate the bitmap if imageType is 1
            val rotatedBitmap = if (imageType == 1) {
                rotateBitmap(bitmap, -90f)
            } else {
                bitmap
            }

            // Set the rotated bitmap to ImageView
            imageView.setImageBitmap(rotatedBitmap)

        } catch (e: IOException) {
            e.printStackTrace()
            // Optionally, handle the error (e.g., set a default image)
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(angle)
        }
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}
