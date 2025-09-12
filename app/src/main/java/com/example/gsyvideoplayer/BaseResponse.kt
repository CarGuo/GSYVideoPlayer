package com.example.gsyvideoplayer


data class BaseResponse<T>(
    val code: Int = 0,
    val msg: String = "",
    val data: T? = null
)
