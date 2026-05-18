package com.example.gsyvideoplayer.compose.host

internal object DemoSamples {
    const val SAMPLE_URL = "https://res.exexm.com/cw_145225549855002"
    const val SAMPLE_URL_2 =
        "https://9-29-1305988530.cos.ap-shanghai.myqcloud.com/IMG_0382.MP4"
    const val SAMPLE_TITLE = "GSY Compose Sample"

    data class SampleItem(val title: String, val url: String, val cover: String? = null)

    val SAMPLE_LIST: List<SampleItem> = listOf(
        SampleItem(
            title = "样片 #1 · GSY Compose Sample",
            url = SAMPLE_URL,
        ),
        SampleItem(
            title = "样片 #2 · IMG_0382",
            url = SAMPLE_URL_2,
        ),
        SampleItem(
            title = "样片 #3 · GSY Compose Sample（重复）",
            url = SAMPLE_URL,
        ),
        SampleItem(
            title = "样片 #4 · IMG_0382（重复）",
            url = SAMPLE_URL_2,
        ),
        SampleItem(
            title = "样片 #5 · GSY Compose Sample（重复）",
            url = SAMPLE_URL,
        ),
    )
}
