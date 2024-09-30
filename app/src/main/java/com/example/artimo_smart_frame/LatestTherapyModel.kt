package com.example.artimo_smart_frame

data class LatestTherapyModel(
    val result: Result // result는 리스트가 아니라 단일 객체
) {
    data class Result(
        val id: Int = 0,
        val sources: String, // sources는 문자열
        val thumb: String = ""
    )
}
