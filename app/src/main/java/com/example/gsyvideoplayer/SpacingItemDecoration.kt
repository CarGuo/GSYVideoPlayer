package com.example.gsyvideoplayer

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(private val spaceSmall: Int, private val spaceLarge: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        if (itemCount == 1) {
            outRect.left = spaceLarge
            outRect.right = spaceLarge
            return
        }

        when (position) {
            0 -> {
                outRect.left = spaceLarge
                outRect.right = spaceSmall
            }

            itemCount - 1 -> {
                outRect.left = spaceSmall
                outRect.right = spaceLarge

            }

            else -> {
                outRect.left = spaceSmall
                outRect.right = spaceSmall
            }
        }
    }
}
