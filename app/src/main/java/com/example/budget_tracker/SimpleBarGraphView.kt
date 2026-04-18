package com.example.budget_tracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SimpleBarGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var data = listOf<Category>()

    fun setData(newData: List<Category>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val maxSpent = data.maxOfOrNull { it.spent } ?: 1.0
        val barWidth = width.toFloat() / (data.size * 2)
        val maxHeight = height.toFloat() - 100f // Leave space for labels

        data.forEachIndexed { index, category ->
            val barHeight = (category.spent / maxSpent * maxHeight).toFloat()
            val left = index * barWidth * 2 + barWidth / 2
            val top = height - barHeight - 60f
            val right = left + barWidth
            val bottom = height - 60f

            paint.color = Color.parseColor("#6200EE")
            canvas.drawRect(left, top, right, bottom, paint)

            paint.color = Color.BLACK
            paint.textSize = 30f
            paint.textAlign = Paint.Align.CENTER
            val label = if (category.name.length > 5) category.name.substring(0, 5) + ".." else category.name
            canvas.drawText(label, left + barWidth / 2, height - 20f, paint)
            
            paint.textSize = 25f
            canvas.drawText("$${category.spent.toInt()}", left + barWidth / 2, top - 10f, paint)
        }
    }
}
