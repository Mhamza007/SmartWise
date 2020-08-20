package com.stackbuffers.groceryclient.activities.beverages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.signin.SignInActivity
import com.stackbuffers.groceryclient.activities.signup.AddPhoneNumberActivity
import com.stackbuffers.groceryclient.model.Cart
import com.stackbuffers.groceryclient.model.Product
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import kotlinx.android.synthetic.main.activity_item_details.*
import java.lang.Exception

class ItemDetailsActivity : AppCompatActivity() {

    private var productId: String? = ""
    private var product: Product? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String

    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        auth = FirebaseAuth.getInstance()
        sharedPreference = SharedPreference(this)
        currentUserId = sharedPreference.getUserId()!!

        try {
            productId = intent.getStringExtra("product_id")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val productsRef = FirebaseDatabase.getInstance().getReference("/Products/$productId")
        productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ItemDetailsActivity,
                    getString(R.string.db_er),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                product = snapshot.getValue(Product::class.java)

                if (product != null) {
                    GlideApp.with(this@ItemDetailsActivity).load(product!!.Product_image)
                        .placeholder(R.drawable.tea_beverages).into(image)
                    name.text = product!!.Product_Name
                    price.text = product!!.Product_Price
                    quantity.text = product!!.Unit
                    description.text = product!!.Description
                }
            }
        })

        //check if item is in cart
        val cartRef = FirebaseDatabase.getInstance().reference
        cartRef.child("Cart").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(productId!!)) {
                        addToCartBtn.visibility = View.GONE
                        itemInCartBtn.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ItemDetailsActivity, "Database Error", Toast.LENGTH_SHORT)
                        .show()
                }

            })

        addToCartBtn.setOnClickListener {
            // check if user is signed in
            if (auth.currentUser != null) {
                Log.d(TAG, "Current User Id $currentUserId")
                val db =
                    FirebaseDatabase.getInstance().getReference("/users")
                db.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val number =
                            snapshot.child(currentUserId).child("Mobile_Number").value.toString()
                        Log.d(TAG, "Phone Number is $number")
                        if (number != "") {
                            // user is signed in with mobile number
                            addProductToCart(product)
                        } else {
                            Toast.makeText(
                                this@ItemDetailsActivity,
                                "Mobile Number is required for adding product to cart",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(
                                Intent(
                                    this@ItemDetailsActivity,
                                    AddPhoneNumberActivity::class.java
                                )
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@ItemDetailsActivity,
                            "Database Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
            } else {
                // user is not signed in
                Toast.makeText(this, "Sign in or Sign up to continue", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@ItemDetailsActivity, SignInActivity::class.java))
            }
        }
    }

    private fun addProductToCart(product: Product?) {
        val cartRef = FirebaseDatabase.getInstance().reference
        //check if item is already in cart
        if (productId != null) {
            cartRef.child("Cart").child(currentUserId).child(productId!!).setValue(
                Cart(productId!!, "${System.currentTimeMillis()}")
            ).addOnCompleteListener {
                Toast.makeText(
                    this@ItemDetailsActivity,
                    "${product!!.Product_Name} Added to cart",
                    Toast.LENGTH_SHORT
                ).show()
                addToCartBtn.visibility = View.GONE
                itemInCartBtn.visibility = View.VISIBLE
            }.addOnFailureListener {
                Toast.makeText(
                    this@ItemDetailsActivity,
                    "Can't add product to cart, Try Again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        const val TAG = "ItemDetailsActivity"
    }
}