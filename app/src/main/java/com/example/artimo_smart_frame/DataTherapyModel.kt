package com.example.artimo_smart_frame

data class DataTherapyModel(
    val result: List<Result> = listOf()
) {
    data class Result(
        val createdAt: String = "",
        val id: Int = 0,
        val sources: List<Source>,
        val thumb: String = "",
    )

    data class Source(
        val infoComment: String = "",
        val videoUrl: String = ""
    )
}
