package com.moneyweather.ui.carouselview

import com.moneyweather.ui.carouselview.helper.EndlessListener
import com.moneyweather.ui.carouselview.helper.ViewHelper
import android.content.Context
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.moneyweather.ui.carouselview.CarouselAdapter
import com.moneyweather.ui.carouselview.CarouselLayoutManager
import com.moneyweather.ui.carouselview.CarouselLazyLoadListener
import com.moneyweather.ui.carouselview.CarouselListener
import com.moneyweather.ui.carouselview.CarouselModel
import com.moneyweather.ui.carouselview.CarouselView


class Carousel constructor(
    private var context: Context,
    @NonNull private var carouselView: CarouselView,
    @NonNull var adapter: CarouselAdapter
) {

    private var manager: CarouselLayoutManager? = null
    private var carouselLazyLoadListener: CarouselLazyLoadListener? = null

    init {
        carouselView.layoutManager = getManager()
        carouselView.adapter = adapter
        carouselView.isAutoScroll = false
    }

    /**
     * @param orientation set VERTICAL/HORIZONTAL
     * @param reverseLayout set RTL layout
     */
    fun setOrientation(
        @CarouselView.CarouselOrientation orientation: Int, reverseLayout: Boolean,
        enablePadding: Boolean = true
    ) {
        manager = CarouselLayoutManager(context, orientation, reverseLayout)
        carouselView.layoutManager = manager
        val padding: Int
        when (orientation) {
            CarouselView.HORIZONTAL -> {
                padding = if (enablePadding) ViewHelper.getScreenWidth() / 4 else 1
                carouselView.setPadding(padding, 0, padding, 0)
            }
            CarouselView.VERTICAL -> {
                padding = if (enablePadding) ViewHelper.getScreenHeight() / 4 else 1
                carouselView.setPadding(0, padding, 0, padding)
            }
        }
    }

    fun addCarouselListener(listener: CarouselListener) {
        carouselView.listener = listener
    }

    fun removeCarouselListener() {
        carouselView.listener = null
    }

    /**
     * lazyLoad load more item with infinity scroll.
     * for enable this feature should be pass true value in first parameter
     * and pass child of CarouselLazyLoadListener for second parameter
     * for disable this feature should be pass false value in first argument
     * and pass null for second parameter
     *
     * @param lazy this flag enable or disable lazy loading view
     * @param carouselLazyLoadListener listener when need call load more item
     */
    fun lazyLoad(lazy: Boolean, carouselLazyLoadListener: CarouselLazyLoadListener?) {
        this.carouselLazyLoadListener = carouselLazyLoadListener

        if (lazy)
            carouselView.addOnScrollListener(object : EndlessListener(getManager()!!) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                    carouselLazyLoadListener?.onLoadMore(page, totalItemsCount, view as CarouselView)
                }
            })
        else
            carouselView.clearOnScrollListeners()
    }

    /**
     * @param scaleView enable scaleView item
     */
    fun scaleView(scaleView: Boolean) {
        getManager()?.scaleView(scaleView)
    }

    /**
     * @return CarouselLayoutManager
     */
    private fun getManager(): CarouselLayoutManager? {
        if (manager == null) setOrientation(CarouselView.VERTICAL, false)
        return manager
    }

    /**
     * @param items list items should be add to carousel
     */
    fun addAll(items: MutableList<CarouselModel>) {
        adapter.addAll(items)
    }

    /**
     * @param item one item should be add to carousel
     */
    fun add(item: CarouselModel) {
        adapter.operation(item, CarouselAdapter.ADD)
    }

    /**
     * @param item list items should be remove to carousel
     */
    fun remove(item: CarouselModel) {
        adapter.operation(item, CarouselAdapter.REMOVE)
    }

    /**
     * @param currentPosition
     */
    fun setCurrentPosition(currentPosition: Int) {
        carouselView.scrollToPosition(currentPosition)
    }

    /**
     * @return current item position
     */
    fun getCurrentPosition(): Int {
        return carouselView.currentPosition
    }

    /**
     * pause auto scrolling
     */
    fun pauseAutoScroll() {
        carouselView.pauseAutoScroll()
    }

    /**
     * resume auto scrolling
     */
    fun resumeAutoScroll() {
        carouselView.resumeAutoScroll()
    }

    /**
     * @param autoScroll
     * @param delayMillis
     * @param loopMode
     */
    fun autoScroll(autoScroll: Boolean, delayMillis: Long, loopMode: Boolean) {
        carouselView.isAutoScroll = autoScroll
        carouselView.delayMillis = delayMillis
        carouselView.isLoopMode = loopMode
    }

    /**
     * @param enableSlider enable slider mode
     */
    fun enableSlider(enableSlider: Boolean) {
        if (getManager()?.orientation == CarouselView.VERTICAL)
            throw IllegalStateException("for using slider mode, orientation must be is HORIZONTAL")
        else
            adapter.enableSlider(enableSlider)
    }

    /**
     * @param scrollSpeed change speed scrolling item
     */
    fun scrollSpeed(scrollSpeed: Float) {
        getManager()?.setScrollSpeed(scrollSpeed)
    }

}