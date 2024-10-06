package com.example.artimo_smart_frame

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class ArtframeActivity : FragmentActivity() {
    private lateinit var therapyApiService: TherapyApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artframe) // Set the layout

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // 기본 URL 설정
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        therapyApiService = retrofit.create(TherapyApiService::class.java)

        // 받아온거 가져오기
        val imageFileName = intent.getStringExtra("art")
        val imageType = intent.getStringExtra("type")?.toIntOrNull()
        val hue = intent.getStringExtra("hue") ?: "hue"
        val saturation = intent.getStringExtra("saturation") ?: "saturation"

        // ImageView에 넣기
        val imageView: ImageView = findViewById(R.id.artframe)

        try {
            // 이미지 열기
            val inputStream = assets.open(imageFileName ?: "")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // imageType이 1이면 돌리기(세로 이미지임)
            val rotatedBitmap = if (imageType == 1) {
                rotateBitmap(bitmap, -90f)
            } else {
                bitmap
            }

            imageView.setImageBitmap(rotatedBitmap)

            val croppedBitmap = cropToAspectRatio(rotatedBitmap, 16, 9)
            imageView.setImageBitmap(croppedBitmap)

        } catch (e: IOException) {
            e.printStackTrace()
            // Optionally, handle the error (e.g., set a default image)
        }

        //IoT 제어
        CoroutineScope(Dispatchers.IO).launch {
            // IoT control
            processIoT(hue, saturation)
        }
    }

    private suspend fun processIoT(hue: String, saturation: String) {
        // local.properties의 id가져오기
        val userId = BuildConfig.USER_ID
        // local.properties의 id가져오기
        val iotId = BuildConfig.IOT_ID

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $iotId") // Add the Authorization header
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.smartthings.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(TherapyApiService::class.java)

        //command 만들기
        val command = SmartThingsCommand(
            commands = listOf(
                Command(
                    component = "main",
                    capability = "colorControl",
                    command = "setColor",
                    arguments = listOf(
                        mapOf(
                            "hue" to hue.toInt(),
                            "saturation" to saturation.toInt()
                        )
                    )
                )
            )
        )
        try {
            val apiResponse = apiService.sendDeviceCommand(userId, command)
            if (apiResponse.isSuccessful) {
                Log.d("SetIoT", "Iot 적용 성공: ${apiResponse.body()}")
            } else {
                Log.d("SetIoT", "Iot 적용 실패: ${apiResponse.errorBody()?.string()}")
            }
        } catch (e: IOException) {
            Log.d("SetIoT", "Network error: ${e.message}")
        } catch (e: HttpException) {
            Log.d("SetIoT", "HTTP error: ${e.message}")
        } catch (e: Exception) {
            Log.d("SetIoT", "Unknown error: ${e.message}")
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

    private fun cropToAspectRatio(bitmap: Bitmap, screenWidth: Int, screenHeight: Int): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        // 새로운 비율 설정 (16:8 비율 기준)
        val targetAspectRatio = 16f / 8f
        val bitmapAspectRatio = bitmapWidth.toFloat() / bitmapHeight.toFloat()

        var cropWidth = bitmapWidth
        var cropHeight = bitmapHeight

        if (bitmapAspectRatio > targetAspectRatio) {
            // 이미지의 가로가 더 넓으면 가로를 자름 (상하 여백 조정)
            cropWidth = (bitmapHeight * targetAspectRatio).toInt()
        } else {
            // 이미지의 세로가 더 길면 세로를 자름 (좌우 여백 조정)
            cropHeight = (bitmapWidth / targetAspectRatio).toInt()
        }

        // 중앙을 기준으로 이미지를 자르고, 여백을 균일하게 맞추기
        val xOffset = (bitmapWidth - cropWidth) / 2
        val yOffset = (bitmapHeight - cropHeight) / 2

        return Bitmap.createBitmap(bitmap, xOffset, yOffset, cropWidth, cropHeight)
    }

}
