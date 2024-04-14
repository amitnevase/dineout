package com.deserve.dineout.presentation.list

import com.deserve.dineout.domain.model.Restaurant

interface OnRestaurantItemClickListener {
    fun onRestaurantItemClick(restaurant: Restaurant)
}