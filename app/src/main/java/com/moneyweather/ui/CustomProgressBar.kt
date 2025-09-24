package com.moneyweather.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.moneyweather.R

class CustomProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Float = 0f

    // 시작과 끝 점
    private var startEndPointSize: Float = 8f.dpToPx()
    private val startEndColor = resources.getColor(R.color.sunrise_progressbar_dot_color, null)

    // 진행 바
    private var barThickness: Float = 6f.dpToPx()
    private val barColor = resources.getColor(R.color.sunrise_progressbar_color, null)

    // 진행 상태 이미지
    private var progressIconSize: Float = 35f.dpToPx()
    private var iconBitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_weather_1)

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2

        // 시작과 끝 여백 계산 (아이콘 크기 고려)
        val startOffset = maxOf(startEndPointSize / 2, progressIconSize / 2)
        val endOffset = maxOf(startEndPointSize / 2, progressIconSize / 2)

        // 배경 바
        paint.color = barColor
        canvas.drawRect(
            startOffset,
            centerY - barThickness / 2,
            width - endOffset,
            centerY + barThickness / 2,
            paint
        )

        // 진행 바
        paint.color = resources.getColor(R.color.sunrise_progressbar_color2, null)
        canvas.drawRect(
            startOffset,
            centerY - barThickness / 2,
            progress * (width - startOffset - endOffset) + startOffset,
            centerY + barThickness / 2,
            paint
        )

        // 시작 점
        paint.color = startEndColor
        canvas.drawCircle(
            startOffset,
            centerY,
            startEndPointSize / 2,
            paint
        )

        // 끝 점
        canvas.drawCircle(
            width - endOffset,
            centerY,
            startEndPointSize / 2,
            paint
        )

        // 진행 상태 아이콘
        val iconX = progress * (width - startOffset - endOffset) + startOffset - progressIconSize / 2
        val iconY = centerY - progressIconSize / 2

        canvas.drawBitmap(
            Bitmap.createScaledBitmap(
                iconBitmap,
                progressIconSize.toInt(),
                progressIconSize.toInt(),
                true
            ),
            iconX,
            iconY,
            paint
        )
    }

    /**
     * 진행 상태 아이콘 설정
     * @param id
     */
    fun setImage(id: Int) {
        iconBitmap = BitmapFactory.decodeResource(resources, id)
    }

    /**
     * 진행률 설정
     * @param value
     */
    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f) // 0.0 ~ 1.0
        invalidate()
    }

    private fun Float.dpToPx() = this * context.resources.displayMetrics.density
}