package com.moneyweather.ui.carouselview


interface CarouselLazyLoadListener {

    fun onLoadMore(page: Int, totalItemsCount: Int, view: CarouselView)

}
