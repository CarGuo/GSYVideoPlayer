package com.example.gsyvideoplayer

import android.graphics.Rect
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.example.gsyvideoplayer.databinding.ItemAiVideoBinding
import kotlin.math.max
import kotlin.math.min


/**
 * ================================================
 * 作    者：Weirdo_lin
 * 版    本：1.0
 * 创建日期：2021/11/15 17:16
 * 描    述：
 * ================================================
 */
class VideoAdapter() : BaseQuickAdapter<TabBean, BaseDataBindingHolder<ItemAiVideoBinding>>(R.layout.item_ai_video) {

    private var mAdapterMap: MutableMap<Int, VideoItemAdapter> = mutableMapOf()

    companion object {
        val spacingItemDecoration = SpacingItemDecoration(16, 32)
    }

    override fun convert(holder: BaseDataBindingHolder<ItemAiVideoBinding>, item: TabBean) {
        val dataBinding = holder.dataBinding
        if (dataBinding != null) {
            val videoItemAdapter = VideoItemAdapter()
            mAdapterMap.put(holder.layoutPosition, videoItemAdapter)
            videoItemAdapter.deliveryType = item.type ?: 1
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            dataBinding.recyclerView.apply {
                layoutManager = linearLayoutManager
                setHasFixedSize(true)
                adapter = videoItemAdapter
            }
            dataBinding.recyclerView.setItemViewCacheSize(30)
            dataBinding.recyclerView.removeItemDecoration(spacingItemDecoration)
            dataBinding.recyclerView.addItemDecoration(spacingItemDecoration)
            dataBinding.recyclerView.addOnScrollListener(HorizontalScrollListener(0.85F, true, linearLayoutManager, { recyclerView, dx, dy ->
                Log.e("视频内层", "默认回调")
            }, {
                videoItemAdapter.startVideo(holder.layoutPosition, it)
                Log.e("视频内层", "新增:${it}")
            }, {
                videoItemAdapter.stopVideo(holder.layoutPosition, it)
                Log.e("视频内层", "移除:${it}")
            }, {
                Log.e("视频内层", "所有可见:${it}")
            }))
            videoItemAdapter.setList(item.list)
        }
    }

    /**
     * 新增行
     */
    fun addNewVideo(newItem: MutableList<Int>) {
        try {
            newItem.forEach { addIndex ->
                val itemView = getViewByPosition(addIndex, R.id.recycler_view)
                if (itemView != null) {
                    val recyclerView = itemView as RecyclerView
                    val findVisibleItems = findVisibleItems(0.85F, recyclerView)
                    mAdapterMap[addIndex]?.startVideo(addIndex, findVisibleItems)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取可见项
     */
    fun findVisibleItems(percent: Float, recyclerView: RecyclerView): MutableList<Int> {
        val visibleItems = mutableListOf<Int>()
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        if (layoutManager.childCount == 0) return visibleItems
        val parentRect = Rect()
        recyclerView.getGlobalVisibleRect(parentRect)
        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val childRect = Rect()
            child.getGlobalVisibleRect(childRect)
            // 计算可见宽度（改为水平方向）
            val visibleWidth = min(childRect.right, parentRect.right) - max(childRect.left, parentRect.left)
            val visibleRatio = visibleWidth.toFloat() / child.width.toFloat()
            if (visibleRatio >= percent) {
                layoutManager.getPosition(child).takeIf { it != RecyclerView.NO_POSITION }?.let {
                    visibleItems.add(it)
                }
            }
        }
        return visibleItems
    }

    /**
     * 移除行
     */
    fun removeVideo(newItem: MutableList<Int>) {
        try {
            newItem.forEach { removeIndex ->
                val videoStateBeans = AppConstants.sVideoPlayMap.values.filter { it.categoryIndex == removeIndex }
                videoStateBeans.forEach { item ->
                    item.videoView?.release()
                    val itemKey = "video_${item.categoryIndex}_${item.enhanceIndex}"
                    AppConstants.sVideoPlayMap.remove(itemKey)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
