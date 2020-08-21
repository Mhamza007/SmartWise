package com.stackbuffers.groceryclient.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.model.Product
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.ItemDecoration
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.item_cart.view.*

class CartActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference
    lateinit var productsList: ArrayList<Product>
    var finalPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        sharedPreference = SharedPreference(this)
        productsList = ArrayList()

        back.setOnClickListener {
            finish()
        }

        val cartRef = FirebaseDatabase.getInstance().getReference("/Cart")
        val productsRef = FirebaseDatabase.getInstance().getReference("/Products")

        cartRef.child(sharedPreference.getUserId()!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val cartItemAdapter = GroupAdapter<GroupieViewHolder>()

                    snapshot.children.forEach {
                        productsRef.child(it.key!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    productsList.add(
                                        Product(
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
                                    )

                                    val productPrice =
                                        snapshot.child("Product_Price").value.toString()
                                    var discountedPrice =
                                        snapshot.child("Discount_Price").value.toString()

                                    if (discountedPrice == "") {
                                        discountedPrice = productPrice
                                    }

                                    finalPrice += discountedPrice.toDouble()

                                    subTotal.text = finalPrice.toString()
                                    checkoutPrice.text = finalPrice.toString()

                                    cartItemAdapter.add(
                                        CartItem(
                                            this@CartActivity,
                                            snapshot,
                                            productsList
                                        )
                                    )
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Utils.dbErToast(this@CartActivity)
                                }
                            })
                    }
                    cartItemList.addItemDecoration(ItemDecoration(50))
                    cartItemList.layoutManager = LinearLayoutManager(this@CartActivity)
                    cartItemList.adapter = cartItemAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(this@CartActivity)
            }
        })

        couponsBtn.setOnClickListener {
            startActivity(Intent(this@CartActivity, CouponsActivity::class.java))
        }

        checkoutLayout.setOnClickListener {
            val ordersRef = FirebaseDatabase.getInstance().getReference("/Orders")

        }
    }

    companion object {
        const val TAG = "CartActivity"
    }

    inner class CartItem(
        private val context: Context,
        private val snapshot: DataSnapshot,
        private var productsList: ArrayList<Product>
    ) :
        Item<GroupieViewHolder>() {

        private val rs = context.getString(R.string.rs)
        private val productPrice = snapshot.child("Product_Price").value.toString()
        private val discountedPrice = snapshot.child("Discount_Price").value.toString()
        private var quantity: Int = 1

        private val cartRef = FirebaseDatabase.getInstance().getReference("/Cart")

        override fun getLayout(): Int {
            return R.layout.item_cart
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            GlideApp.with(context).load(snapshot.child("Product_image").value)
                .into(viewHolder.itemView.image)
            viewHolder.itemView.name.text = snapshot.child("Product_Name").value.toString()
            if (discountedPrice != "") {
                viewHolder.itemView.oldPrice.text = rs + productPrice
                viewHolder.itemView.newPrice.text = rs + discountedPrice
            } else {
                viewHolder.itemView.oldPrice.visibility = View.GONE
                viewHolder.itemView.newPrice.text = rs + productPrice
            }
            viewHolder.itemView.item_quantity.text = quantity.toString()

            viewHolder.itemView.addCartBtn.setOnClickListener {
                quantity++
                viewHolder.itemView.item_quantity.text = quantity.toString()

                cartRef.child(sharedPreference.getUserId()!!)
                    .child(productsList[position].Product_ID).child("quantity").setValue(quantity)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            var disPrice = productsList[position].Discount_Price
                            val proPrice = productsList[position].Product_Price

                            if (disPrice == "") disPrice = proPrice

                            finalPrice += disPrice.toDouble()

                            subTotal.text = finalPrice.toString()
                            checkoutPrice.text = finalPrice.toString()
                        } else
                            Utils.toast(context, "Error")
                    }.addOnFailureListener {
                        Utils.toast(context, "Error")
                    }
            }

            viewHolder.itemView.minusCartBtn.setOnClickListener {
                if (quantity > 0) {
                    quantity--

                    cartRef.child(sharedPreference.getUserId()!!)
                        .child(productsList[position].Product_ID).child("quantity")
                        .setValue(quantity)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                var disPrice = productsList[position].Discount_Price
                                val proPrice = productsList[position].Product_Price

                                if (disPrice == "") disPrice = proPrice

                                finalPrice -= disPrice.toDouble()

                                subTotal.text = finalPrice.toString()
                                checkoutPrice.text = finalPrice.toString()
                            } else
                                Utils.toast(context, "Error")
                        }.addOnFailureListener {
                            Utils.toast(context, "Error")
                        }

                }
                if (quantity >= 0)
                    viewHolder.itemView.item_quantity.text = quantity.toString()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
