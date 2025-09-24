package com.moneyweather.model

import com.moneyweather.ui.carouselview.CarouselModel

class CarouselCard constructor(private var id: Int) : CarouselModel() {

    fun getId(): Int {
        return id
    }
}