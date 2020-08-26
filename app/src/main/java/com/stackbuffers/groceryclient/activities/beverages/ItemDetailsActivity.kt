package com.stackbuffers.groceryclient.activities.beverages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.stackbuffers.groceryclient.model.WishList
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
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
                Utils.dbErToast(this@ItemDetailsActivity)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                product = snapshot.getValue(Product::class.java)

                if (product != null) {
                    GlideApp.with(this@ItemDetailsActivity).load(product!!.Product_image)
                        .placeholder(R.drawable.tea_beverages).into(image)
                    name.text = product!!.Product_Name
                    if (product!!.Discount_Price == "")
                        price.text = product!!.Product_Price
                    else
                        price.text = product!!.Discount_Price
                    item_quantity.text = product!!.Unit
                    description.text = product!!.Description
                }
            }
        })

        val wishListRef = FirebaseDatabase.getInstance().reference

        wishListRef.child("WishList").child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                        if (snapshot.hasChild(productId!!))
                            favourite.setColorFilter(
                                ContextCompat.getColor(
                                    this@ItemDetailsActivity,
                                    R.color.colorPrimary
                                )
                            )
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@ItemDetailsActivity)
                }

            })

        favourite.setOnClickListener {
            wishListRef.child("WishList").child(currentUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // check if item is already in wish list
                        if (snapshot.hasChild(productId!!)) {
                            Utils.toast(this@ItemDetailsActivity, "Item is in wish list")
                            // item is already in wish list
                            wishListRef.child("WishList").child(currentUserId)
                                .child(productId!!)
                                .removeValue()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        favourite.setColorFilter(
                                            ContextCompat.getColor(
                                                this@ItemDetailsActivity,
                                                R.color.colorBg
                                            )
                                        )
                                        Utils.toast(
                                            this@ItemDetailsActivity,
                                            "${product!!.Product_Name} Removed"
                                        )
                                    } else
                                        Utils.toast(this@ItemDetailsActivity, "Error")
                                }.addOnFailureListener {
                                    Utils.toast(this@ItemDetailsActivity, "Error")
                                    Log.e(TAG, "Error Adding to wish list ${it.message}")
                                }
                        } else {
                            wishListRef.child("WishList").child(currentUserId)
                                .child(productId!!).setValue(
                                    WishList(
                                        productId!!,
                                        "${System.currentTimeMillis()}"
                                    )
                                ).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        favourite.setColorFilter(
                                            ContextCompat.getColor(
                                                this@ItemDetailsActivity,
                                                R.color.colorPrimary
                                            )
                                        )
                                        Utils.toast(
                                            this@ItemDetailsActivity,
                                            "${product!!.Product_Name} added to wish list"
                                        )
                                    } else
                                        Utils.toast(this@ItemDetailsActivity, "Error")
                                }.addOnFailureListener {
                                    Utils.toast(this@ItemDetailsActivity, "Error")
                                    Log.e(TAG, "Error Adding to wish list ${it.message}")
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Utils.dbErToast(this@ItemDetailsActivity)
                    }
                })
        }

        //check if item is in cart
        val cartRef = FirebaseDatabase.getInstance().reference
        cartRef.child("Cart").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                        if (snapshot.hasChild(productId!!)) {
                            addToCartBtn.visibility = View.GONE
                            itemInCartBtn.visibility = View.VISIBLE
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@ItemDetailsActivity)
                }

            })

        addToCartBtn.setOnClickListener {
            // check if user is signed in
            if (auth.currentUser != null) {
                Log.d(TAG, "Current User Id $currentUserId")
                val userRef =
                    FirebaseDatabase.getInstance().getReference("/users")
                userRef.addValueEventListener(object : ValueEventListener {
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
                        Utils.dbErToast(this@ItemDetailsActivity)
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
        val time = "${System.currentTimeMillis()}"
        val cartRef = FirebaseDatabase.getInstance().reference
        if (product != null)
            if (productId != null) {
                val price =
                    if (product.Discount_Price == "") product.Product_Price else product.Discount_Price
                cartRef.child("Cart").child(currentUserId).child(productId!!)
                    .setValue(
                        Cart(
                            productId!!,
                            time,
                            product.Product_Name,
                            product.Product_image,
                            price,
                            1
                        )
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Utils.toast(
                                this@ItemDetailsActivity,
                                "${product.Product_Name} Added to cart"
                            )
                            addToCartBtn.visibility = View.GONE
                            itemInCartBtn.visibility = View.VISIBLE
                        } else {
                            Utils.toast(
                                this@ItemDetailsActivity,
                                "Can't add product to cart, Try Again"
                            )
                        }
                    }.addOnFailureListener {
                        Utils.toast(
                            this@ItemDetailsActivity,
                            "Can't add product to cart, Try Again"
                        )
                    }
            }
    }

    companion object {
        const val TAG = "ItemDetailsActivity"
    }
}