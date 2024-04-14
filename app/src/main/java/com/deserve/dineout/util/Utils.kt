package com.deserve.dineout.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object Utils {
    suspend fun loadImageFromUrl(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun isLocationEnabled(requireContext: Context): Boolean {
        val locationManager =
            requireContext.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun getRestaurantImageUrl(icon: String): String {
        return "${Constant.BASE_URL}photo?maxwidth=400" +
                "&photoreference=$icon" +
                "&key=${Constant.YOUR_API_KEY}"
    }

    fun getRatingAsterisks(rating: Double): String {
        val maxRating = 5 // Assuming the maximum rating is 5
        val maxAsterisks = 5 // Maximum number of asterisks to represent the rating

        // Calculate the number of asterisks to display based on the rating
        val numAsterisks = ((rating / maxRating) * maxAsterisks).toInt()

        // Construct the string of asterisks
        return "*".repeat(numAsterisks)
    }
}