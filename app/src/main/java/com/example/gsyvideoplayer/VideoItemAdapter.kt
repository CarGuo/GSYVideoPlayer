package com.example.gsyvideoplayer

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.example.gsyvideoplayer.databinding.ItemAiVideoItemBinding
import com.shuyu.gsyvideoplayer.utils.GSYVideoType


/**
 * ================================================
 * 作    者：Weirdo_lin
 * 版    本：1.0
 * 创建日期：2021/11/15 17:16
 * 描    述：
 * ================================================
 */
class VideoItemAdapter : BaseQuickAdapter<DataListBean, BaseDataBindingHolder<ItemAiVideoItemBinding>>(R.layout.item_ai_video_item) {

    var deliveryType: Int = 0

    var onHomeItemClickListener: OnHomeItemClickListener? = null

    override fun convert(holder: BaseDataBindingHolder<ItemAiVideoItemBinding>, item: DataListBean) {
        val dataBinding = holder.dataBinding
        if (dataBinding != null) {
            //根据单列和双列类型，设置不同布局
            val layoutParams = dataBinding.cvItem.layoutParams as RecyclerView.LayoutParams
            if (deliveryType == 0) {
                layoutParams.width = context.resources.getDimensionPixelOffset(R.dimen.margin_250)
                layoutParams.height = context.resources.getDimensionPixelOffset(R.dimen.margin_150)
            } else {
                layoutParams.width = context.resources.getDimensionPixelOffset(R.dimen.margin_150)
                layoutParams.height = context.resources.getDimensionPixelOffset(R.dimen.margin_200)
            }
            dataBinding.cvItem.layoutParams = layoutParams
            //设置图片或视频
            if (item.photoType == 2) {
                //视频
                dataBinding.videoView.visibility = View.VISIBLE
                dataBinding.ivImage.visibility = View.GONE
                initVideoView(dataBinding.videoView, item, holder.layoutPosition)
            } else {
                dataBinding.videoView.visibility = View.GONE
                dataBinding.ivImage.visibility = View.VISIBLE
                Glide.with(context).load(item.originPicUrl).into(dataBinding.ivImage)
            }
        }
    }

    /**
     * 初始化VideoView
     */
    private fun initVideoView(videoPlayer: MultiVideoView, dataListBean: DataListBean, position: Int) {
        //先销毁
        val videoUrl = dataListBean.originPicUrl
        val videoKey = "video_" + dataListBean.portraitStyleVo?.portraitStyleName + "_" + dataListBean.portraitStyleVo?.portraitStyleId
        //再创建
        videoPlayer.playPosition = position
        videoPlayer.playTag = videoKey
        videoPlayer.isReleaseWhenLossAudio = false
        videoPlayer.loadCoverImage(videoUrl ?: "", 0)
        videoPlayer.setUp(videoUrl, true, "")
        videoPlayer.isLooping = true
        //切换渲染模式
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL)
    }

    /**
     * 停止视频
     */
    fun stopVideo(categoryIndex: Int, hideVideos: MutableList<Int>) {
        try {
            hideVideos.forEach {
                val enhanceInfoBean = data[it]
                if (enhanceInfoBean.photoType == 2) {
                    val mapKey = "video_${categoryIndex}_${it}"
                    val stateBean = AppConstants.sVideoPlayMap[mapKey]
                    if (stateBean == null) {
                        return
                    }
                    stateBean.videoView?.release()
                    AppConstants.sVideoPlayMap.remove(mapKey)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 播放视频
     */
    fun startVideo(categoryIndex: Int, startVideos: MutableList<Int>) {

        Handler(Looper.myLooper()!!).postDelayed({
            try {
                startVideos.forEach {
                    val enhanceInfoBean = data[it]
                    if (enhanceInfoBean.photoType == 2) {
                        //视频才做播放操作
                        val itemValue = getViewByPosition(it, R.id.video_view)
                        if (itemValue != null) {
                            val videoView = itemValue as MultiVideoView
                            val videoStateBean = VideoStateBean(categoryIndex, it, videoView, true)
                            val itemKey = "video_${categoryIndex}_${it}"
                            AppConstants.sVideoPlayMap.put(itemKey, videoStateBean)
                            videoView.startPlayLogic()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 50)

    }


    interface OnHomeItemClickListener {
        fun onItemClickListener(enhanceInfoBean: DataListBean)
    }
}
