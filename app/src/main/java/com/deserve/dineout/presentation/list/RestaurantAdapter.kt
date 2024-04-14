package com.deserve.dineout.presentation.list// RestaurantAdapter.kt

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.deserve.dineout.R
import com.deserve.dineout.databinding.ItemRestaurantBinding
import com.deserve.dineout.domain.model.Restaurant
import com.deserve.dineout.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RestaurantAdapter(
    val mListener: OnRestaurantItemClickListener
) :
    RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    private val restaurantList: ArrayList<Restaurant> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurantList[position]
        holder.bind(restaurant)
    }

    override fun getItemCount(): Int {
        return restaurantList.size
    }

    fun addData(list: List<Restaurant>) {
        this.restaurantList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val itemBinding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(restaurant: Restaurant) {
            itemBinding.nameTextView.text = restaurant.name
            GlobalScope.launch(Dispatchers.Main) {
                val bitmap = Utils.loadImageFromUrl(Utils.getRestaurantImageUrl(restaurant.icon))
                bitmap?.let {
                    itemBinding.imageView.setImageBitmap(it)
                }
            }

            itemBinding.openStatusTextView.apply {
                if (restaurant.isOpen != null) {
                    isVisible = true
                    text = restaurant.isOpen
                    if (restaurant.isOpen == itemBinding.root.context.getString(R.string.open))
                        setTextColor(Color.GREEN)
                    else
                        setTextColor(Color.RED)
                } else {
                    isVisible = false
                }
            }
            itemView.setOnClickListener {
                mListener.onRestaurantItemClick(restaurant)
            }
        }
    }
}


