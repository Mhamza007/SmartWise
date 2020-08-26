package com.stackbuffers.groceryclient.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.beverages.ItemDetailsActivity
import com.stackbuffers.groceryclient.activities.signin.SignInActivity
import com.stackbuffers.groceryclient.activities.signup.AddPhoneNumberActivity
import com.stackbuffers.groceryclient.model.Cart
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_promotions.*
import kotlinx.android.synthetic.main.item_offer.view.*

class PromotionsActivity : AppCompatActivity() {

    private val promotionRef = FirebaseDatabase.getInstance().getReference("/Promotion")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promotions)

        back.setOnClickListener {
            finish()
        }

        promotionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offersAdapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    if (it.hasChild("Product_ID")) {
                        offersAdapter.add(OfferItem(this@PromotionsActivity, it))
                    }
                }

                offersList.layoutManager = LinearLayoutManager(this@PromotionsActivity)
                offersList.adapter = offersAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(this@PromotionsActivity)
            }
        })
    }
}

class OfferItem(private val context: Context, private val dataSnapshot: DataSnapshot) :
    Item<GroupieViewHolder>() {
    private val auth = FirebaseAuth.getInstance()
    private val sharedPreference = SharedPreference(context)
    private val cartRef = FirebaseDatabase.getInstance().getReference("/Cart")

    override fun getLayout() = R.layout.item_offer

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            with(viewHolder.itemView) {
                val productId = dataSnapshot.child("Product_ID").value.toString()
                val productImage = dataSnapshot.child("Product_image").value.toString()
                val productName = dataSnapshot.child("Product_Name").value.toString()
                val productPrice = dataSnapshot.child("Product_Price").value.toString()
                val discountedPrice = dataSnapshot.child("Discount_Price").value.toString()

                GlideApp.with(context).load(productImage).into(image)
                name.text = productName
                if (discountedPrice == "") {
                    oldPrice.visibility = View.GONE
                    newPrice.text = productPrice
                } else {
                    oldPrice.text = productPrice
                    newPrice.text = discountedPrice
                }
                addToCartBtn.setOnClickListener {
                    checkIfUserExists()
                }
                this.setOnClickListener {
                    val intent = Intent(context, ItemDetailsActivity::class.java)
                    intent.putExtra("product_id", productId)
                    context.startActivity(intent)
                }
            }
        }
    }

    private fun checkIfUserExists() {
        if (auth.currentUser != null) {
            val userRef =
                FirebaseDatabase.getInstance().getReference("/users")
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val number =
                        snapshot.child(sharedPreference.getUserId()!!)
                            .child("Mobile_Number").value.toString()
                    Log.d(ItemDetailsActivity.TAG, "Phone Number is $number")
                    if (number != "") {
                        // user is signed in with mobile number
                        addProductToCart()
                    } else {
                        Toast.makeText(
                            context,
                            "Mobile Number is required for adding product to cart",
                            Toast.LENGTH_SHORT
                        ).show()
                        context.startActivity(
                            Intent(
                                context,
                                AddPhoneNumberActivity::class.java
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(context)
                }

            })
        } else {
            // user is not signed in
            Utils.toast(context, "Sign in or Sign up to continue")
            context.startActivity(Intent(context, SignInActivity::class.java))
        }
    }

    private fun addProductToCart() {
        val productId = dataSnapshot.child("Product_ID").value.toString()
        val productImage = dataSnapshot.child("Product_image").value.toString()
        val productName = dataSnapshot.child("Product_Name").value.toString()
        val productPrice = dataSnapshot.child("Product_Price").value.toString()
        val discountedPrice = dataSnapshot.child("Discount_Price").value.toString()
        val time = "${System.currentTimeMillis()}"

        val price: String
        price = if (discountedPrice == "") productPrice else discountedPrice

        cartRef.child(sharedPreference.getUserId()!!)
            .child(productId)
            .setValue(
                Cart(
                    productId,
                    time,
                    productName,
                    productImage,
                    price,
                    1
                )
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    Utils.toast(context, "$productName Added to cart")
                } else {
                    Utils.toast(context, "Can't add product to cart, Try Again")
                }
            }.addOnFailureListener {
                Utils.toast(context, "Can't add product to cart, Try Again")
            }
    }
}