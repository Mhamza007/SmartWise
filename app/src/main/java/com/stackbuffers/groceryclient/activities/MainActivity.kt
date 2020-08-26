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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.orders.MyOrdersActivity
import com.stackbuffers.groceryclient.activities.signin.SignInActivity
import com.stackbuffers.groceryclient.activities.signup.SignUpActivity
import com.stackbuffers.groceryclient.fragments.HomeFragment
import com.stackbuffers.groceryclient.fragments.PromotionsFragment
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_header.*
import kotlinx.android.synthetic.main.nav_drawer_menu.*

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var navigation: BottomNavigationView
    private lateinit var sharedPreference: SharedPreference

    private lateinit var auth: FirebaseAuth
    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")
    private val financeRef = FirebaseDatabase.getInstance().getReference("/Finance")

    override fun onResume() {
        super.onResume()

        if (auth.currentUser == null) {
            exitMenu.visibility = View.GONE
        } else {
            exitMenu.visibility = View.VISIBLE
        }

        if (auth.currentUser != null) {
            loginSignupMenu.visibility = View.GONE
            usersRef.child(sharedPreference.getUserId()!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userName.text = snapshot.child("Name").value.toString()
                        GlideApp.with(this@MainActivity)
                            .load(snapshot.child("profileImageUrl").value)
                            .placeholder(R.drawable.profile_image).into(userImage)
                        if (snapshot.hasChild("Points"))
                            userPts.text = snapshot.child("Points").value.toString()
                        else
                            userPts.text = "0"
                        if (snapshot.hasChild("orderCount")) {
                            val orderCount = snapshot.child("orderCount").value.toString().toInt()
                            financeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {

                                        val silverStart =
                                            snapshot.child("SilverStart").value.toString().toInt()
                                        val silverEnd =
                                            snapshot.child("SilverEnd").value.toString().toInt()
                                        val goldStart =
                                            snapshot.child("GoldStart").value.toString().toInt()
                                        val goldEnd =
                                            snapshot.child("GoldEnd").value.toString().toInt()
                                        val platStart =
                                            snapshot.child("PlatinumStart").value.toString().toInt()
                                        val platEnd =
                                            snapshot.child("PlatinumEnd").value.toString().toInt()

                                        when (orderCount) {
                                            in silverStart until silverEnd -> {
                                                GlideApp.with(this@MainActivity)
                                                    .load(R.drawable.silver_coin).into(coin)
                                                userPoints.text = getString(R.string.silver)
                                            }
                                            in goldStart until goldEnd -> {
                                                GlideApp.with(this@MainActivity)
                                                    .load(R.drawable.gold_coin).into(coin)
                                                userPoints.text = getString(R.string.gold)
                                            }
                                            in platStart until platEnd -> {
                                                GlideApp.with(this@MainActivity)
                                                    .load(R.drawable.plat_coin).into(coin)
                                                userPoints.text = getString(R.string.plat)
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Utils.dbErToast(this@MainActivity)
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Utils.dbErToast(this@MainActivity)
                    }
                })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val newToken = instanceIdResult.token
            FirebaseMessaging.getInstance().subscribeToTopic("Notifications").addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Subscribed for notifications")
                } else {
                    Log.e(TAG, "Unable to Subscribe for notifications")
                }
            }.addOnFailureListener {
                Log.e(TAG, "Unable to Subscribe for notifications ${it.message}")
            }
        }

        auth = FirebaseAuth.getInstance()
        sharedPreference = SharedPreference(this@MainActivity)

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

        search.setOnClickListener {
        }

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

        loginSignupMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, SignUpActivity::class.java))
        }

        myProfileMenu.setOnClickListener {
            if (auth.currentUser != null)
                startActivity(Intent(this@MainActivity, MyProfileActivity::class.java))
            else
                startActivity(Intent(this@MainActivity, SignUpActivity::class.java))
        }

        myCartMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, CartActivity::class.java))
        }

        myOrdersMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, MyOrdersActivity::class.java))
        }

        favoriteMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, WishListActivity::class.java))
        }

        returnItemMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, ReturnActivity::class.java))
        }

        couponsMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, CouponsActivity::class.java))
        }

        shareCartMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, ShareCartActivity::class.java))
        }

        helpMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, HelpActivity::class.java))
        }

        contactUsMenu.setOnClickListener {
            startActivity(Intent(this@MainActivity, ContactUsActivity::class.java))
        }

        exitMenu.setOnClickListener {
            auth.signOut().also {
                val intent = Intent(this@MainActivity, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
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
                R.id.action_spin -> {
                    startActivity(Intent(this, SpinWinActivity::class.java))
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
                .add(R.id.frame_container, PromotionsFragment(), OFFERS).commit()
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

        const val HOME = "HOME"
        const val OFFERS = "OFFERS"
        const val CART = "CART"
        const val ME = "ME"
    }
}
