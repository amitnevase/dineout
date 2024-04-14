package com.deserve.dineout.presentation.list

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.deserve.dineout.domain.model.Restaurant
import com.deserve.dineout.domain.repository.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestaurantListViewModel : ViewModel() {
    var repository = MainRepository()
    var nextPageToken: String? = null
    private val _nearbyRestaurants = MutableLiveData<List<Restaurant>>()
    val nearbyRestaurants: MutableLiveData<List<Restaurant>>
        get() = _nearbyRestaurants
    val errorResponseLiveData = MutableLiveData<String>()

    fun fetchNearbyRestaurants(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.getNearbyRestaurants(latitude, longitude, nextPageToken)
                if (response != null) {
                    nextPageToken = response.nextPageToken
                    _nearbyRestaurants.postValue(response.restaurants)
                }
            } catch (e: Exception) {
                errorResponseLiveData.postValue("error")
            }
        }
    }
}
