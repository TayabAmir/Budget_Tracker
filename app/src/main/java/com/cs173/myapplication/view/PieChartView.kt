package com.cs173.myapplication.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.cs173.myapplication.AppData

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val totalSpent = AppData.expenses.sumOf { it.amount }
        if (totalSpent == 0.0) return

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = Math.min(width, height) / 2 * 0.8f
        rectF.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius)

        var startAngle = 0f

        for (category in AppData.categories) {
            val categorySpent = AppData.expenses.filter { it.categoryId == category.id }.sumOf { it.amount }
            if (categorySpent > 0) {
                val sweepAngle = (categorySpent / totalSpent * 360).toFloat()
                paint.color = Color.parseColor(category.colorHex)
                canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
                startAngle += sweepAngle
            }
        }
    }
}
