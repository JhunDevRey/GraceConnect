package com.example.graceconnect

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import kotlin.math.max

/**
 * Lays children out in equal-width columns (the count usually comes from a width-qualified resource),
 * stretching every child in a row to the tallest one so cards line up.
 */
class ColumnGridLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    var columns = 1
        set(value) {
            field = value.coerceAtLeast(1)
            requestLayout()
        }
    private var columnSpacing = 0
    private var rowSpacing = 0
    private var cellWidth = 0

    init {
        context.withStyledAttributes(attrs, R.styleable.ColumnGridLayout) {
            columns = getInt(R.styleable.ColumnGridLayout_gridColumns, 1)
            columnSpacing = getDimensionPixelSize(R.styleable.ColumnGridLayout_gridColumnSpacing, 0)
            rowSpacing = getDimensionPixelSize(R.styleable.ColumnGridLayout_gridRowSpacing, 0)
        }
    }

    private fun rows() = children.filter { it.visibility != View.GONE }.toList().chunked(columns)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        cellWidth = ((width - paddingLeft - paddingRight - columnSpacing * (columns - 1)) / columns).coerceAtLeast(0)

        var height = paddingTop + paddingBottom
        rows().forEachIndexed { index, row ->
            var rowHeight = 0
            row.forEach { child ->
                val lp = child.layoutParams as MarginLayoutParams
                child.measure(widthSpecFor(lp), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                rowHeight = max(rowHeight, child.measuredHeight + lp.topMargin + lp.bottomMargin)
            }
            row.forEach { child ->
                val lp = child.layoutParams as MarginLayoutParams
                val target = rowHeight - lp.topMargin - lp.bottomMargin
                if (child.measuredHeight != target) {
                    child.measure(widthSpecFor(lp), MeasureSpec.makeMeasureSpec(target, MeasureSpec.EXACTLY))
                }
            }
            height += rowHeight + if (index > 0) rowSpacing else 0
        }
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec))
    }

    private fun widthSpecFor(lp: MarginLayoutParams) =
        MeasureSpec.makeMeasureSpec((cellWidth - lp.leftMargin - lp.rightMargin).coerceAtLeast(0), MeasureSpec.EXACTLY)

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val rtl = layoutDirection == View.LAYOUT_DIRECTION_RTL
        var top = paddingTop
        rows().forEachIndexed { index, row ->
            if (index > 0) top += rowSpacing
            var rowHeight = 0
            row.forEachIndexed { column, child ->
                val lp = child.layoutParams as MarginLayoutParams
                val offset = column * (cellWidth + columnSpacing)
                val cellLeft = if (rtl) width - paddingRight - offset - cellWidth else paddingLeft + offset
                val left = cellLeft + lp.leftMargin
                val childTop = top + lp.topMargin
                child.layout(left, childTop, left + child.measuredWidth, childTop + child.measuredHeight)
                rowHeight = max(rowHeight, child.measuredHeight + lp.topMargin + lp.bottomMargin)
            }
            top += rowHeight
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(context, attrs)
    override fun generateDefaultLayoutParams() = MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    override fun generateLayoutParams(p: LayoutParams?) = MarginLayoutParams(p)
    override fun checkLayoutParams(p: LayoutParams?) = p is MarginLayoutParams
}

/** A LinearLayout that never grows wider than [contentMaxWidth], so content stays readable on tablets. */
class MaxWidthLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    var contentMaxWidth = 0
        set(value) {
            field = value
            requestLayout()
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.MaxWidthLinearLayout) {
            contentMaxWidth = getDimensionPixelSize(R.styleable.MaxWidthLinearLayout_contentMaxWidth, 0)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec)
        val spec = if (contentMaxWidth > 0 && size > contentMaxWidth) {
            val mode = MeasureSpec.getMode(widthMeasureSpec)
            MeasureSpec.makeMeasureSpec(contentMaxWidth, if (mode == MeasureSpec.EXACTLY) mode else MeasureSpec.AT_MOST)
        } else {
            widthMeasureSpec
        }
        super.onMeasure(spec, heightMeasureSpec)
    }
}
