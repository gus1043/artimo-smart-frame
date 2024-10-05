package com.example.artimo_smart_frame

data class LatestTherapyModel(
    val result: Result
) {
    data class Result(
        val id: Int = 0,
        val sources: Sources,
        val thumb: String = ""
    )

    data class Sources(
        val infoComment: String = "",
        val videoUrl: String = ""
    )
}