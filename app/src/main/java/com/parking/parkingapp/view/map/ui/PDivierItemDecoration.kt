package com.parking.parkingapp.view.map.ui

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView

class PDivierItemDecoration(private val divider: Drawable): RecyclerView.ItemDecoration() {
    override fun onDrawOver(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDrawOver(c, parent, state)

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val position = params.viewAdapterPosition
            if (position != RecyclerView.NO_POSITION && position < (parent.adapter?.itemCount?.minus(
                    1
                ) ?: return)
            ) {
                val left = child.left + params.leftMargin
                val right = child.right - params.rightMargin
                val top = child.bottom + params.bottomMargin
                val bottom = top + divider.intrinsicHeight

                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }
        }
    }
}