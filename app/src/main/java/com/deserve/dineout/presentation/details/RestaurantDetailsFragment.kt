package com.deserve.dineout.presentation.details

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.deserve.dineout.R
import com.deserve.dineout.domain.model.Restaurant
import com.deserve.dineout.databinding.FragmentRestaurantDetailsBinding
import com.deserve.dineout.presentation.main.MainActivity
import com.deserve.dineout.util.Constant.RESTAURANT
import com.deserve.dineout.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RestaurantDetailsFragment : Fragment() {

    private lateinit var binding: FragmentRestaurantDetailsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRestaurantDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val restaurant: Restaurant? = arguments?.getParcelable(RESTAURANT)
        setRestaurantDetails(restaurant)
    }

    private fun setRestaurantDetails(restaurant: Restaurant?) {
        GlobalScope.launch(Dispatchers.Main) {
            val bitmap = Utils.loadImageFromUrl(Utils.getRestaurantImageUrl(restaurant?.icon?:""))
            bitmap?.let {
                binding.restaurantImage.setImageBitmap(it)
            }
        }
        (activity as MainActivity).setToolBarTitle(restaurant?.name)
        binding.restaurantName.text = restaurant?.name
        binding.ratingText.text = getString(R.string.rating)+Utils.getRatingAsterisks(restaurant?.rating?:0.0)
        binding.addressText.text = getString(R.string.address, restaurant?.vicinity)
        if (restaurant?.isOpen != null) {
            binding.openStatusText.isVisible = true
            binding.openStatusText.text = restaurant.isOpen
            if (restaurant.isOpen == getString(R.string.open))
                binding.openStatusText.setTextColor(Color.GREEN)
            else
                binding.openStatusText.setTextColor(Color.RED)
        } else {
            binding.openStatusText.isVisible = false
        }
        binding.getDirectionsButton.setOnClickListener {
            openGoogleMapsForDirections(
                requireContext(),
                restaurant?.geometry?.location?.lat ?: 0.0,
                restaurant?.geometry?.location?.lng ?: 0.0
            )
        }
    }

    private fun openGoogleMapsForDirections(
        context: Context,
        latitude: Double,
        longitude: Double
    ) {
        val googleMapsUrl = "google.navigation:q=$latitude,$longitude"
        val uri = Uri.parse(googleMapsUrl)

        val googleMapsPackage = "com.google.android.apps.maps"
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(googleMapsPackage)
        }

        context.startActivity(intent)
    }
}