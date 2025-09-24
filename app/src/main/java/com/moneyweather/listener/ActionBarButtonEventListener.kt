package com.moneyweather.listener

import com.moneyweather.model.enums.ActionBarRightButtonEnum
import com.moneyweather.model.enums.ActionBarRightSecondButtonEnum

interface ActionBarButtonEventListener {
    fun onClickActionBarLeftButton()

    fun onClickActionBarRightButton(rightSecondButtonEnum: ActionBarRightButtonEnum)

    fun onClickActionBarRightSecondButton(rightSecondSecondButtonEnum: ActionBarRightSecondButtonEnum)

    fun onClickActionBarTitle()

    fun onSearchStart(keyword: String)

    fun onSearchClose()
}