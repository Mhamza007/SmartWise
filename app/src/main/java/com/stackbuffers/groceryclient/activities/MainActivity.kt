package com.stackbuffers.groceryclient.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.signup.SignUpActivity
import com.stackbuffers.groceryclient.fragments.HomeFragment
import com.stackbuffers.groceryclient.fragments.OffersFragment
import com.stackbuffers.groceryclient.model.Product
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_header.*
import kotlinx.android.synthetic.main.nav_drawer_menu.*

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var navigation: BottomNavigationView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        navigation = findViewById(R.id.bottomNavBar)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (savedInstanceState == null) {
            navigation.selectedItemId = R.id.action_home
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_container, HomeFragment(), HOME)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        notifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        userImage.setOnClickListener {
            startActivity(Intent(this@MainActivity, MyProfileActivity::class.java))
        }

        userName.setOnClickListener {
            startActivity(Intent(this@MainActivity, MyProfileActivity::class.java))
        }

        closeNav.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        points.setOnClickListener {
            startActivity(Intent(this@MainActivity, PointsActivity::class.java))
        }

        if (auth.currentUser != null) {
            loginSignupMenu.visibility = View.GONE
        }

        loginSignupMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, SignUpActivity::class.java))
        }

        myProfileMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, MyProfileActivity::class.java))
        }

        myCartMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, CartActivity::class.java))
        }

        myOrdersMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, OrdersActivity::class.java))
        }

        favoriteMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, WishListActivity::class.java))
        }

        couponsMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, CouponsActivity::class.java))
        }

        helpMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, HelpActivity::class.java))
        }

        contactUsMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, ContactUsActivity::class.java))
        }

        productList = ArrayList()
    }

    private var mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_offers -> {
                    Log.d(TAG, "Navigation: Offers")
                    toolbarText.text = getString(R.string.offers)
                    showOffersFragment()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_home -> {
                    Log.d(TAG, "Navigation: Home")
                    toolbarText.text = getString(R.string.home)
                    showHomeFragment()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_menu -> {
                    drawerLayout.openDrawer(GravityCompat.START)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private fun showOffersFragment() {
        if (supportFragmentManager.findFragmentByTag(HOME) != null) {
            supportFragmentManager.beginTransaction()
                .hide(supportFragmentManager.findFragmentByTag(HOME)!!)
                .commit()
        }
        if (supportFragmentManager.findFragmentByTag(CART) != null) {
            supportFragmentManager.beginTransaction()
                .hide(supportFragmentManager.findFragmentByTag(CART)!!)
                .commit()
        }
        if (supportFragmentManager.findFragmentByTag(ME) != null) {
            supportFragmentManager.beginTransaction()
                .hide(supportFragmentManager.findFragmentByTag(ME)!!)
                .commit()
        }
        if (supportFragmentManager.findFragmentByTag(OFFERS) != null) {
            supportFragmentManager.beginTransaction()
                .show(supportFragmentManager.findFragmentByTag(OFFERS)!!)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .add(R.id.frame_container, OffersFragment(), OFFERS).commit()
        }
    }

    private fun showHomeFragment() {
        if (supportFragmentManager.findFragmentByTag(OFFERS) != null) {
            supportFragmentManager.beginTransaction()
                .hide(supportFragmentManager.findFragmentByTag(OFFERS)!!)
                .commit()
        }
        if (supportFragmentManager.findFragmentByTag(CART) != null) {
            supportFragmentManager.beginTransaction()
                .hide(supportFragmentManager.findFragmentByTag(CART)!!)
                .commit()
        }
        if (supportFragmentManager.findFragmentByTag(ME) != null) {
            supportFragmentManager.beginTransaction()
                .hide(supportFragmentManager.findFragmentByTag(ME)!!)
                .commit()
        }
        if (supportFragmentManager.findFragmentByTag(HOME) != null) {
            supportFragmentManager.beginTransaction()
                .show(supportFragmentManager.findFragmentByTag(HOME)!!)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .add(R.id.frame_container, HomeFragment(), HOME).commit()
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            showHomeFragment()
            super.onBackPressed()
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val HOME = "HOME"
        private const val OFFERS = "OFFERS"
        private const val CART = "CART"
        private const val ME = "ME"

        lateinit var productList: ArrayList<Product>
    }
}
