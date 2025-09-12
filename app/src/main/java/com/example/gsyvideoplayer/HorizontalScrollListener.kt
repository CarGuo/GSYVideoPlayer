package com.example.gsyvideoplayer

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class HorizontalScrollListener(
    private val percent: Float = 0.85F, private val isStopScrollListener: Boolean, private val layoutManager: RecyclerView.LayoutManager, private val onScrolled: (recyclerView: RecyclerView, dx: Int, dy: Int) -> Unit, private val newItems: (list: MutableList<Int>) -> Unit, private val hiddenItems: (list: MutableList<Int>) -> Unit, private val allVisibleItems: (list: MutableList<Int>) -> Unit
) : RecyclerView.OnScrollListener() {

    private var lastVisibleItems = mutableSetOf<Int>()

    private var isScrollingFast = false

    private var lastScrollTime = 0L

    private var lastScrollX = 0

    private var mIsInt: Boolean = true //是否初始化

    private val scrollThreshold: Int = 25 // 快速滑动阈值(像素)

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        when (newState) {

            RecyclerView.SCROLL_STATE_IDLE -> {
                isScrollingFast = false
                // 滑动停止时检查一次可见项
                checkVisibleItems(true, recyclerView)
            }

            RecyclerView.SCROLL_STATE_DRAGGING -> {
                // 用户开始拖动时重置状态
                isScrollingFast = false
            }
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (mIsInt) {
            mIsInt = false
            if (isStopScrollListener) {
                checkVisibleItems(false, recyclerView)
            } else {
                checkVisibleItems(true, recyclerView)
            }
        }
        if (isStopScrollListener) {
            return
        }
        // 检测快速滑动（改为检测水平滚动dx）
        val now = System.currentTimeMillis()
        val timeDiff = now - lastScrollTime
        val scrollDiff = abs(dx)  // 使用dx而不是dy

        if (timeDiff < 100 && scrollDiff > scrollThreshold) {
            isScrollingFast = true
        }

        lastScrollTime = now
        lastScrollX += dx  // 记录水平滚动位置

        // 如果不是快速滑动才检查可见项
        if (!isScrollingFast) {
            checkVisibleItems(true, recyclerView)
        }
    }

    private fun checkVisibleItems(isNeedCallback: Boolean, recyclerView: RecyclerView) {
        val currentVisibleItems = findVisibleItems(recyclerView)

        // 如果没有变化，则不回调
        if (currentVisibleItems == lastVisibleItems) {
            return
        }

        // 找出新增的item
        val addedItems = currentVisibleItems.filter { !lastVisibleItems.contains(it) }

        // 找出移除的item
        val removedItems = lastVisibleItems.filter { !currentVisibleItems.contains(it) }

        // 更新记录的上次可见item集合
        lastVisibleItems = currentVisibleItems.toMutableSet()
        // 触发回调
        if (isNeedCallback) {
            newItems.invoke(addedItems.toMutableList())
            hiddenItems.invoke(removedItems.toMutableList())
            allVisibleItems.invoke(currentVisibleItems.toMutableList())
        }
    }

    private fun findVisibleItems(recyclerView: RecyclerView): Set<Int> {
        val visibleItems = mutableSetOf<Int>()
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

    fun resetVisibleItems() {
        lastVisibleItems.clear()
        isScrollingFast = false
    }
}
