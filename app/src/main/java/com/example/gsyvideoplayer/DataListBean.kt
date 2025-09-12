package com.example.gsyvideoplayer

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

data class DataListBean(
    var portraitStyleVo: DataListBean? = null,
    var portraitStyleName: String? = "",
    var portraitStyleId: String? = "",
    var originPicUrl: String? = "",
    var photoType: Int? = 0,
) : Serializable, MultiItemEntity {
    override val itemType: Int
        get() = photoType ?: 0
}
