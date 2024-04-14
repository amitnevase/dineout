package com.deserve.dineout.domain.repository

import android.content.Context
import android.util.Log
import com.deserve.dineout.App
import com.deserve.dineout.R
import com.deserve.dineout.domain.model.Restaurant
import com.deserve.dineout.domain.model.RestaurantResponse
import com.deserve.dineout.util.Constant.BASE_URL
import com.deserve.dineout.util.Constant.YOUR_API_KEY
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainRepository {

    fun getNearbyRestaurants(
        latitude: Double,
        longitude: Double,
        nextPageToken: String?
    ): RestaurantResponse? {
        val urlString = buildUrlString(latitude, longitude, nextPageToken)

        // Open connection
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Read the response
        val inputStream = connection.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            response.append(line)
            line = reader.readLine()
        }

        return processResponse(response.toString())
    }

    private fun buildUrlString(
        latitude: Double,
        longitude: Double,
        nextPageToken: String?
    ): String {
        val urlString = StringBuilder(BASE_URL+"nearbysearch/json")
        urlString.append("?location=$latitude,$longitude")
        urlString.append("&radius=5000")
        urlString.append("&type=restaurant")
        urlString.append("&key=$YOUR_API_KEY")
        if (nextPageToken != null) {
            urlString.append("&pagetoken=$nextPageToken")
        }
        return urlString.toString()
    }

    private fun readJsonFromAssets(context: Context, fileName: String): String? {
        return try {
            // Open the JSON file
            val inputStream = context.assets.open(fileName)

            // Read the JSON data as a String
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun processResponse(response: String): RestaurantResponse? {
        Log.e("GOOGLE","Response = "+response)
        try {

//            TODO remove after full implementation
//            val jsonString = readJsonFromAssets(App.appContext!!, "restaurants.json")

            return response?.let { parseJson(it) }
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    private fun parseJson(jsonString: String): RestaurantResponse {
        val jsonObject = JSONObject(jsonString)
        val nextPageToken = jsonObject.getString("next_page_token")
        val resultsArray = jsonObject.getJSONArray("results")
        val restaurants = mutableListOf<Restaurant>()
        var isOpenNow: String? = null

        for (i in 0 until resultsArray.length()) {
            val result = resultsArray.getJSONObject(i)
            val name = result.getString("name")
            // Get opening_hours object
            // Get opening_hours object
            val openingHoursObject: JSONObject? = result.optJSONObject("opening_hours")
            if (openingHoursObject != null) {
                // Get open_now
                isOpenNow = getRestaurantOpenStatus(openingHoursObject.optBoolean("open_now"))
            }
            // Extract the "photos" array if present
            val photosArray = result.optJSONArray("photos")
            val photoUrl: String = if (photosArray != null && photosArray.length() > 0) {
                val photoObject = photosArray.getJSONObject(0)
                photoObject.optString("photo_reference")
            } else {
                ""
            }
            val rating = result.getDouble("rating")
            val vicinity = result.getString("vicinity")
            val geometryObject: JSONObject = result.getJSONObject("geometry")
            val locationObject = geometryObject.getJSONObject("location")
            val latitude = locationObject.getDouble("lat")
            val longitude = locationObject.getDouble("lng")
            val location = Restaurant.Location(latitude, longitude)
            val geometry = Restaurant.Geometry(location)
            val restaurant = Restaurant(name, rating, vicinity, photoUrl, geometry, isOpenNow)
            restaurants.add(restaurant)
        }

        return RestaurantResponse(restaurants, nextPageToken)
    }

    private fun getRestaurantOpenStatus(isOpen: Boolean): String? {
        return if (isOpen) {
            App.appContext?.getString(R.string.open)
        } else if (!isOpen) {
            App.appContext?.getString(R.string.closed)
        } else {
            null
        }
    }
}


