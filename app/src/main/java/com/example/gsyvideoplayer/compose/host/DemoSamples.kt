package com.example.gsyvideoplayer.compose.host

import com.example.gsyvideoplayer.utils.DemoVideoUrls

internal object DemoSamples {
    const val SAMPLE_URL = DemoVideoUrls.SAMPLE_GSY
    const val SAMPLE_URL_2 = DemoVideoUrls.MP4_BBB
    const val SAMPLE_TITLE = "GSY Compose Sample"

    data class SampleItem(val title: String, val url: String, val cover: String? = null)

    val SAMPLE_LIST: List<SampleItem> = listOf(
        SampleItem(
            title = "样片 #1 · GSY Compose Sample",
            url = SAMPLE_URL,
        ),
        SampleItem(
            title = "样片 #2 · BBB",
            url = SAMPLE_URL_2,
        ),
        SampleItem(
            title = "样片 #3 · GSY Compose Sample（重复）",
            url = SAMPLE_URL,
        ),
        SampleItem(
            title = "样片 #4 · BBB（重复）",
            url = SAMPLE_URL_2,
        ),
        SampleItem(
            title = "样片 #5 · GSY Compose Sample（重复）",
            url = SAMPLE_URL,
        ),
    )
}
