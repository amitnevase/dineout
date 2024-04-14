package com.deserve.dineout.presentation.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.deserve.dineout.R
import com.deserve.dineout.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.activity_restaurants_list_fragment) as NavHostFragment
        // Instantiate the navController using the NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment -> {
                    supportActionBar?.hide()
                    binding.appBar.visibility = View.GONE
                }

                else -> {
                    supportActionBar?.show()
                    binding.appBar.visibility = View.VISIBLE
                }
            }
        }

        // App bar config for toolbar visibility
        val appBarConfiguration = AppBarConfiguration
            .Builder(
                R.id.splashFragment,
                R.id.restaurantListFragment,
                R.id.restaurantDetailsFragment
            )
            .build()

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    // for toolbar title from other child screens
    fun setToolBarTitle(title: String?) {
        binding.toolbarTitle.text = title
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
