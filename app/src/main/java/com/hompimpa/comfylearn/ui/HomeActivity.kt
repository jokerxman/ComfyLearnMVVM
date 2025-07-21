package com.hompimpa.comfylearn.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == RESULT_OK) recreate() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupObservers()
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.appBarHome.toolbar)
        supportActionBar?.title = ""

        val drawerLayout = binding.drawerLayout
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_study, R.id.nav_games, R.id.nav_learnprog),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    viewModel.onSettingsClicked()
                    true
                }

                else -> NavigationUI.onNavDestinationSelected(menuItem, navController)
            }
        }
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(this) { user ->
            updateUserHeader(user)
        }
        viewModel.navigationEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { navEvent ->
                val intent = Intent(this, navEvent.targetClass)
                if (navEvent.forResult) {
                    settingsLauncher.launch(intent)
                } else {
                    startActivity(intent)
                }
                if (navEvent.finishActivity) {
                    finish()
                }
            }
        }
    }

    private fun updateUserHeader(user: FirebaseUser?) {
        val headerView = binding.navView.getHeaderView(0)
        val userNameTextView: TextView = headerView.findViewById(R.id.name_text)
        val userEmailTextView: TextView = headerView.findViewById(R.id.email_text)
        val userProfileImageView: ImageView = headerView.findViewById(R.id.imageView)

        if (user != null) {
            userNameTextView.text =
                user.displayName?.takeIf { it.isNotBlank() } ?: getString(R.string.no_name)
            userEmailTextView.text = user.email ?: getString(R.string.no_email)
            Glide.with(this).load(user.photoUrl).placeholder(R.mipmap.ic_launcher_round)
                .circleCrop().into(userProfileImageView)
        } else {
            userNameTextView.text = getString(R.string.no_name)
            userEmailTextView.text = getString(R.string.no_email)
            Glide.with(this).load(R.mipmap.ic_launcher_round).circleCrop()
                .into(userProfileImageView)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}