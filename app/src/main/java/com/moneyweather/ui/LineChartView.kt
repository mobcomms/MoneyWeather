package com.moneyweather.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.moneyweather.R

class LineChartView (context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    companion object {
        private val POINT_RADIUS = 5f
    }

    private var dataList: ArrayList<Int>? = null
    private val paint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    private val linePath = Path()
    private val dotList = ArrayList<PointF>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas ?: return

        // 1. 기준이 되는 x축 라인 그리기
        /*
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.WHITE
        val cy = height.toFloat() / 2f
        canvas.drawLine(0f, cy, width.toFloat(), cy, paint)*/

        // 2. 각 데이터를 선으로 이어서 그리기
        paint.color = context.getColor(R.color.hour_weather_line_color)
        paint.strokeWidth = 2f
        canvas.drawPath(linePath, paint)

        // 3. 라인위에 점 표시하기
        paint.color = context.getColor(R.color.day_week_color)
        paint.strokeWidth = POINT_RADIUS * 2
        dotList.forEach { dot ->
            canvas.drawPoint(dot.x, dot.y, paint)
        }
    }

    fun setDataList(dataList: ArrayList<Int>) {
        if (dataList.isEmpty()) {
            return
        }
        this.dataList = dataList
        // 이때는 아직 View의 Layout이 아직 결정되기 전일수도 있으므로 listener를 등록한다.
        addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                removeOnLayoutChangeListener(this)

                val maxValue = dataList.maxOrNull() ?: return
                var scale = 1f
                val halfHeight = height / 2
                // 값이 뷰 영역을 벗어나지 않고 화면내에 그려질수 있도록 비율 계산
                if (maxValue > halfHeight - POINT_RADIUS) {
                    scale = (halfHeight - POINT_RADIUS) / maxValue
                }

                // 값이 가운데 위치한 x축을 기준으로 표시되도록 변환시켜 줌
                val mappedDataList = dataList.map { value -> halfHeight -  value * scale }

                var x = POINT_RADIUS // 각 값이 위치할 x좌표
                val space = (width - POINT_RADIUS * 2) / (mappedDataList.count() - 1) // 한 화면에 모두 표시하기 위해 데이터간 거리를 width기준으로 계산
                linePath.reset()
                dotList.clear()
                mappedDataList.forEach { value ->
                    if (linePath.isEmpty) {
                        linePath.moveTo(x, value)
                    } else {
                        linePath.lineTo(x, value)
                    }
                    dotList.add(PointF(x, value))
                    x += space // 다음 값을 표시할 x좌표 설정
                }
                invalidate() // onDraw()가 다시 불릴수 있도록 갱신 요청
            }
        })
        requestLayout()
    }
}