package com.example.gsyvideoplayer

import java.io.Serializable

data class TabBean(
    val id: String? = "",
    val list: MutableList<DataListBean>? = mutableListOf(),
    val type: Int? = 1,
) : Serializable
