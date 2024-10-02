package com.example.artimo_smart_frame

data class DataTherapyModel(
    val result: List<Result> = listOf()
) {
    data class Result(
        val createdAt: String = "",
        val id: Int = 0,
        val sources: List<String>,
        val thumb: String = "",
    )
}