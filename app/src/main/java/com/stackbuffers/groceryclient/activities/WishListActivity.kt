package com.stackbuffers.groceryclient.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.model.Cart
import com.stackbuffers.groceryclient.model.Product
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_item_details.*
import kotlinx.android.synthetic.main.activity_wish_list.*
import kotlinx.android.synthetic.main.activity_wish_list.back
import kotlinx.android.synthetic.main.item_wish_list.view.*

class WishListActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference
    val wishListRef = FirebaseDatabase.getInstance().getReference("/WishList")
    val productsRef = FirebaseDatabase.getInstance().getReference("/Products")
    val cartRef = FirebaseDatabase.getInstance().getReference("/Cart")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_list)

        sharedPreference = SharedPreference(this)

        back.setOnClickListener {
            finish()
        }

        wishListRef.child(sharedPreference.getUserId()!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val wishListAdapter = GroupAdapter<GroupieViewHolder>()

                        snapshot.children.forEach {
                            productsRef.child(it.key!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        wishListAdapter.add(
                                            WishListItem(
                                                this@WishListActivity,
                                                snapshot
                                            )
                                        )
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Utils.dbErToast(this@WishListActivity)
                                    }
                                })
                        }
                        wishList.layoutManager = LinearLayoutManager(this@WishListActivity)
                        wishList.adapter = wishListAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@WishListActivity)
                }
            })
    }

    private fun reloadList() {
        val intent = Intent(this@WishListActivity, WishListActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    private fun addProductToCart(productId: String): Boolean {
        var added = false
        val time = System.currentTimeMillis().toString()
        productsRef.child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val product: Product = Product(
                    snapshot.child("Category_ID").value.toString(),
                    snapshot.child("Description").value.toString(),
                    snapshot.child("Discount_Price").value.toString(),
                    snapshot.child("Product_ID").value.toString(),
                    snapshot.child("Product_Name").value.toString(),
                    snapshot.child("Product_Price").value.toString(),
                    snapshot.child("Product_image").value.toString(),
                    snapshot.child("SubCategory_ID").value.toString(),
                    snapshot.child("Unit").value.toString()
                )
                val price =
                    if (product.Discount_Price == "") product.Product_Price else product.Discount_Price
                cartRef.child(sharedPreference.getUserId()!!).child(productId)
                    .setValue(
                        Cart(
                            productId,
                            time,
                            product.Product_Name,
                            product.Product_image,
                            price,
                            1
                        )
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Utils.toast(
                                this@WishListActivity,
                                "${product.Product_Name} Added to cart"
                            )
                            added = true
                        } else {
                            added = false
                        }
                    }.addOnFailureListener {
                        Utils.toast(this@WishListActivity, "Can't add product to cart, Try Again")
                        added = false
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(this@WishListActivity)
            }
        })
        return added
    }


    inner class WishListItem(private val context: Context, private val snapshot: DataSnapshot) :
        Item<GroupieViewHolder>() {

        private val rs = context.getString(R.string.rs)
        private val discountedPrice = snapshot.child("Discount_Price").value.toString()
        private val productPrice = snapshot.child("Product_Price").value.toString()

        override fun getLayout() = R.layout.item_wish_list

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            GlideApp.with(context).load(snapshot.child("Product_image").value)
                .into(viewHolder.itemView.image)
            viewHolder.itemView.name.text = snapshot.child("Product_Name").value.toString()
            if (discountedPrice == "") {
                viewHolder.itemView.price.text = rs + productPrice
            } else {
                viewHolder.itemView.price.text = rs + discountedPrice
            }
            viewHolder.itemView.close.setOnClickListener {
                // remove from wishlist
                wishListRef.child(sharedPreference.getUserId()!!).child(snapshot.key!!)
                    .removeValue()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Utils.toast(context, "Item Removed from wish list")
                            reloadList()
                        } else {
                            Utils.toast(context, "Failed to remove item")
                        }
                    }.addOnFailureListener {
                        Utils.toast(context, "Failed to remove item")
                    }
            }

            viewHolder.itemView.addToCartBtn.setOnClickListener {
                addProductToCart(snapshot.key.toString()).let {
                    if (it) {
                        viewHolder.itemView.addToCartBtn.text =
                            context.getString(R.string.item_in_cart)
                    }
                }
            }
        }
    }
}