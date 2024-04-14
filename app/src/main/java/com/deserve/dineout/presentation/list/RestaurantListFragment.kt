package com.deserve.dineout.presentation.list

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deserve.dineout.R
import com.deserve.dineout.domain.model.Restaurant
import com.deserve.dineout.databinding.FragmentRestaurantListBinding
import com.deserve.dineout.presentation.main.MainActivity
import com.deserve.dineout.util.Constant.PAGE_SIZE
import com.deserve.dineout.util.Constant.PERMISSION_REQUEST_LOCATION
import com.deserve.dineout.util.Constant.REQUEST_ENABLE_LOCATION
import com.deserve.dineout.util.Constant.RESTAURANT
import com.deserve.dineout.util.Utils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest


class RestaurantListFragment : Fragment(), OnRestaurantItemClickListener {
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    private lateinit var binding: FragmentRestaurantListBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: RestaurantAdapter
    private lateinit var viewModel: RestaurantListViewModel

    private var isLoading = false
    private var isLastPage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRestaurantListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[RestaurantListViewModel::class.java]
        initializeComponents()
        checkLocationPermissionAndSettings()
        observeListResponse()
    }

    private fun observeListResponse() {
        viewModel.nearbyRestaurants.observe(this, Observer { restaurants ->
            // Update UI with nearby restaurants
            // Add new data to the list
            restaurants?.let {  adapter.addData(it) }

            // Check if it's the last page
            isLoading = false
            if (viewModel.nextPageToken.isNullOrEmpty()) {
                isLastPage = true
            }
        })

        viewModel.errorResponseLiveData.observe(this) {
            binding.constraintErrorView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun initializeComponents() {
        isLoading = false
        isLastPage = false
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        initialiseRestaurantList()
    }

    private fun initialiseRestaurantList() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RestaurantAdapter( this as OnRestaurantItemClickListener)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE
                    ) {
                        // If there is a nextPageToken, fetch the next page
                        if (viewModel.nextPageToken != null && viewModel.nextPageToken!!.isNotEmpty()) {
                            viewModel.fetchNearbyRestaurants(latitude, longitude)
                        }
                    }
                }
            }
        })
    }

    private fun checkLocationPermissionAndSettings() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        } else {
            // Permission is granted, check if location services are enabled
            requestEnableLocationServices()
        }
    }

    private fun requestEnableLocationServices() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(requireContext())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Location services are already enabled, proceed with fetching location
            fetchLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location services are disabled, but can be enabled
                try {
                    // Show dialog to enable location services
                    exception.startResolutionForResult(
                        requireActivity(), REQUEST_ENABLE_LOCATION
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            } else {
                // Location services are disabled and cannot be enabled, show an error message
                Toast.makeText(requireContext(), "Location services disabled", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            // Check if location services were enabled after the user interaction
            if (Utils.isLocationEnabled(requireContext())) {
                // Location services are enabled, proceed with fetching location
                fetchLocation()
            } else {
                // Location services are still disabled after user interaction, show an error message
                Toast.makeText(
                    requireContext(),
                    "Failed to enable location services",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestEnableLocationServices()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Location permission is required to use this feature.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun fetchLocation() {
        binding.progressBar.isVisible = true // Show progress dialog while fetching location
        try {
            val locationRequest = LocationRequest.create().apply {
                interval = 0
                fastestInterval = 0
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        for (location in locationResult.locations) {
                            // Handle location updates
                            latitude = location.latitude
                            longitude = location.longitude
                            binding.progressBar.isVisible = false
                            // Make the coroutine call to fetch nearby restaurants
                            viewModel.fetchNearbyRestaurants(latitude, longitude)
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }
                },
                null
            )
        } catch (securityException: SecurityException) {
            // Handle SecurityException
            Toast.makeText(
                requireContext(),
                "SecurityException: ${securityException.message}",
                Toast.LENGTH_SHORT
            ).show()
            binding.progressBar.isVisible = false
        }
    }

    override fun onRestaurantItemClick(restaurant: Restaurant) {
        openRestaurantDetails(restaurant)
    }

    // passing Restaurant instance to detail page to show detailed info about character
    private fun openRestaurantDetails(restaurant: Restaurant) {
        val bundle = Bundle()
        bundle.putParcelable(RESTAURANT, restaurant)
        findNavController().navigate(
            R.id.action_restaurantListFragment_to_restaurantDetailsFragment,
            bundle
        )
    }

    // reset toolbar title
    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setToolBarTitle(getString(R.string.app_name))
    }
}