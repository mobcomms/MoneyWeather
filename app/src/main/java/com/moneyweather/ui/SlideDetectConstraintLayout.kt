package com.moneyweather.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.WindowManager
import android.view.animation.PathInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.atan2

class SlideDetectConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val velocityThreshold: Float = 10f
    private val minDragDistanceX: Float = 20f
    private val swipeThresholdRatio: Float = 0.7f
    private val screenWidth: Int
    private val swipeThreshold: Float

    private var velocityTracker: VelocityTracker? = null
    private var targetRecyclerView: RecyclerView? = null
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var currentX: Float = 0f
    private var currentY: Float = 0f
    private var isDragging: Boolean = false
    private var lastX: Float = 0f
    private var lastDx: Float = 0f

    var onSwipeUnlockListener: (() -> Unit)? = null

    companion object {
        const val CURRENT_VELOCITY_UNITS = 1000
        const val SLIDE_BACK_ANIMATION_DURATION = 150L
        const val SLIDE_FINISH_ANIMATION_DURATION = 300L
        const val HORIZONTAL_GESTURE_ANGLE_LIMIT = 30.0

        const val SLIDE_UNLOCK_GESTURE_DX_THRESHOLD = 30
        const val SLIDE_UNLOCK_GESTURE_DY_THRESHOLD = 30
    }

    init {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        swipeThreshold = screenWidth * swipeThresholdRatio
    }

    fun setTargetRecyclerView(rv: RecyclerView) {
        targetRecyclerView = rv
    }

    /**
     * 슬라이드 제스처가 감지될 경우 자식 뷰의 터치 이벤트를 처리하지 않습니다.
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY
                currentX = startX
                isDragging = false
                velocityTracker = VelocityTracker.obtain().apply { addMovement(event) }
                return false
            }

            MotionEvent.ACTION_MOVE -> {
                currentX = event.rawX
                currentY = event.rawY
                val dx = currentX - startX
                val dy = currentY - startY

                if (isSlideUnlockGesture(dx, dy)) {
                    isDragging = true
                    return true
                }
                velocityTracker?.addMovement(event)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.recycle()
                velocityTracker = null
                isDragging = false
                return false
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    /**
     * 슬라이드 동작을 처리합니다.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY
                currentX = startX
                lastX = startX
                isDragging = false
                velocityTracker = VelocityTracker.obtain().apply { addMovement(event) }
            }

            MotionEvent.ACTION_MOVE -> {
                currentX = event.rawX
                currentY = event.rawY
                val dx = currentX - startX
                val dy = currentY - startY

                lastDx = currentX - lastX
                lastX = currentX

                if (!isStay(dx) && isHorizontalGesture(dx, dy)) {
                    isDragging = true
                    translationX = dx
                } else {
                    translationX = 0f
                }

                velocityTracker?.apply {
                    addMovement(event)
                    computeCurrentVelocity(CURRENT_VELOCITY_UNITS)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val dx = currentX - startX
                val dy = currentY - startY
                val velocityX = velocityTracker?.xVelocity ?: 0f

                if (isDragging && isHorizontalGesture(dx, dy) && isFinishSwipeGesture(dx, velocityX)) {
                    animateFinish()
                } else {
                    animateBack()
                }

                isDragging = false
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return true
    }

    /**
     * 오른쪽으로 충분히 드래그한 경우 및
     * 일정 속도 이상으로 빠르게 오른쪽으로 살짝 밀었을 경우 허용
     * @param dx
     * @param velocityX
     * @return
     */
    private fun isFinishSwipeGesture(dx: Float, velocityX: Float): Boolean {
        val absVelocityX = abs(velocityX)
        val isRightward = lastDx > 0
        return (dx > swipeThreshold && absVelocityX <= velocityThreshold) || (isRightward && dx > minDragDistanceX && absVelocityX > velocityThreshold)
    }

    private fun isStay(dx: Float): Boolean = dx <= 10

    /**
     * 각도가 큰 대각선 방향은 막고 수평에 가까운 경우만 허용
     * @param dx
     * @param dy
     * @return
     */
    private fun isHorizontalGesture(dx: Float, dy: Float): Boolean {
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
        return angle in -HORIZONTAL_GESTURE_ANGLE_LIMIT..HORIZONTAL_GESTURE_ANGLE_LIMIT
    }

    /**
     * RecyclerView 왼쪽으로 스크롤이 가능한지 여부
     * @return
     */
    private fun isRecyclerViewBlocking() = targetRecyclerView?.canScrollHorizontally(-1) == true

    /**
     * @param dx
     * @param dy
     * @return
     */
    private fun isSlideUnlockGesture(dx: Float, dy: Float): Boolean {
        return dx > SLIDE_UNLOCK_GESTURE_DX_THRESHOLD && // 오른쪽 방향으로 드래그
                abs(dy) < SLIDE_UNLOCK_GESTURE_DY_THRESHOLD && // 위쪽 방향 제한
                isHorizontalGesture(dx, dy) &&
                !isRecyclerViewBlocking()
    }

    private fun animateFinish() {
        animate()
            .translationX(screenWidth.toFloat())
            .setInterpolator(PathInterpolator(0f, 0f, 0.2f, 1f))
            .setDuration(SLIDE_FINISH_ANIMATION_DURATION)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onSwipeUnlockListener?.invoke()
                }
            })
            .start()
    }

    private fun animateBack() {
        animate()
            .translationX(0f)
            .setDuration(SLIDE_BACK_ANIMATION_DURATION)
            .start()
    }
}