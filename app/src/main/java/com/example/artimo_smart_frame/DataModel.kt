package com.example.artimo_smart_frame

data class DataModel(
    val result: List<Result> = listOf()
) {
    data class Result(
        val id: Int = 0,
        val title: String = "",
        val painter: String = "",
        val image: String = "",
        val type: Int = 0
    )
}