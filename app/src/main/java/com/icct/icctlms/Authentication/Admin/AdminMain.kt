package com.icct.icctlms.Authentication.Admin

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.icct.icctlms.R
import com.icct.icctlms.databinding.ActivityAdminMain2Binding

class AdminMain : AppCompatActivity() {

    private var backPressed  = 0L
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityAdminMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_admin_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.admin_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_admin_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (backPressed + 2000 > System.currentTimeMillis()){
            MaterialAlertDialogBuilder(this)
                .setMessage("Click okay to exit")
                .setNegativeButton("Cancel"){_, _ ->
                }
                .setCancelable(false)
                .setPositiveButton("Okay"){_,_ ->
                    super.onBackPressed()
                }.show()
        }else{
            Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT).show()
        }
        backPressed = System.currentTimeMillis()

    }
}