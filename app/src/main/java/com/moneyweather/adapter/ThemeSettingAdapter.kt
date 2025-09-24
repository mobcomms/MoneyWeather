package com.moneyweather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.moneyweather.R
import com.moneyweather.model.CarouselCard
import com.moneyweather.model.enums.ThemeType
import com.moneyweather.ui.carouselview.CarouselAdapter
import com.moneyweather.util.PrefRepository

class ThemeSettingAdapter  : CarouselAdapter() {

    private val EMPTY_ITEM = 0
    private val NORMAL_ITEM = 1

    private var vh: CarouselAdapter.CarouselViewHolder? = null
    var onClick: OnClick? = null

    fun setOnClickListener(onClick: OnClick?) {
        this.onClick = onClick
    }

    override fun getItemViewType(position: Int): Int {
        return NORMAL_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselAdapter.CarouselViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == NORMAL_ITEM) {
            val v = inflater.inflate(R.layout.item_carousel, parent, false)
            vh = MyViewHolder(v)
            vh as MyViewHolder
        } else {
            val v = inflater.inflate(R.layout.item_carousel, parent, false)
            vh = EmptyMyViewHolder(v)
            vh as EmptyMyViewHolder
        }
    }

    override fun onBindViewHolder(holder: CarouselAdapter.CarouselViewHolder, position: Int) {

                vh = holder
                val model = getItems()[position] as CarouselCard
                var selectedTheme = PrefRepository.SettingInfo.selectThemeType

                when(model.getId()){
                    0 -> {
                        (vh as MyViewHolder).image.setImageResource(R.drawable.img_theme_type_info)
                    }
                    1 -> {
                        (vh as MyViewHolder).image.setImageResource(R.drawable.img_theme_type_simple)
                    }
                    2 -> {
                        (vh as MyViewHolder).image.setImageResource(R.drawable.img_theme_type_calendar)
                    }
                    3 -> {
                        (vh as MyViewHolder).image.setImageResource(R.drawable.img_theme_type_background)
                    }
                    4 -> {
                        (vh as MyViewHolder).image.setImageResource(R.drawable.img_theme_type_video)

                        Glide.with(holder.itemView.context)
                            .asGif()
                            .load(R.drawable.clear)
                            .into((vh as MyViewHolder).video)
                    }
                }

                (vh as MyViewHolder).video.visibility = if(ThemeType.VIDEO.type == model.getId()) View.VISIBLE else View.GONE
                (vh as MyViewHolder).selectedImg.visibility = if(selectedTheme == model.getId()) View.VISIBLE else View.GONE
                (vh as MyViewHolder).guideImg.visibility = if(selectedTheme == model.getId()) View.VISIBLE else View.INVISIBLE

        (vh as MyViewHolder).image.setOnClickListener {
            this.onClick?.let {
                it.click(model)
            }
        }

    }

    inner class MyViewHolder(itemView: View) : CarouselViewHolder(itemView) {
        val video: ImageView = itemView.findViewById(R.id.cardVideo)
        val image: ImageView = itemView.findViewById(R.id.cardImage)
        val selectedImg: ImageView = itemView.findViewById(R.id.selectedImg)
        val guideImg: ImageView = itemView.findViewById(R.id.guide)
    }

    inner class EmptyMyViewHolder(itemView: View) : CarouselViewHolder(itemView) {
    //    var titleEmpty: TextView = itemView.item_empty_text
    }

    interface OnClick {
        fun click(model: CarouselCard)
    }
}