package com.stackbuffers.groceryclient.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.model.Product
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_return_items.*
import kotlinx.android.synthetic.main.item_completed_order_product.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ReturnItemsActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference
    private val ordersRef = FirebaseDatabase.getInstance().getReference("/Orders")
    private val returnRef = FirebaseDatabase.getInstance().getReference("/Return")
    private var orderId: String? = null
    private lateinit var productsList: ArrayList<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_items)

        productsList = ArrayList()
        sharedPreference = SharedPreference(this@ReturnItemsActivity)

        try {
            orderId = intent.getStringExtra("order_id")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        back.setOnClickListener {
            finish()
        }

        orderId?.let {
            ordersRef.child(sharedPreference.getUserId()!!).child(orderId!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dateInMillis = snapshot.child("date").value.toString()
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = dateInMillis.toLong()
                            val formatter = SimpleDateFormat("dd-MM-yyyy")
                            date.text = formatter.format(calendar.time)
                            price.text = snapshot.child("totalPrice").value.toString()
                            countProducts.text = snapshot.child("Products").childrenCount.toString()

                            ordersRef.child(sharedPreference.getUserId()!!).child(orderId!!)
                                .child("Products").addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val adapter = GroupAdapter<GroupieViewHolder>()
                                            snapshot.children.forEach {
                                                productsList.add(
                                                    Product(
                                                        "",
                                                        "",
                                                        snapshot.child("productPrice").value.toString(),
                                                        snapshot.child("productId").value.toString(),
                                                        snapshot.child("productName").value.toString(),
                                                        snapshot.child("productPrice").value.toString(),
                                                        snapshot.child("productImage").value.toString(),
                                                        "",
                                                        ""
                                                    )
                                                )
                                                adapter.add(
                                                    OrderProductItem(
                                                        this@ReturnItemsActivity,
                                                        it,
                                                        productsList
                                                    )
                                                )
                                            }
                                            orderProductsList.layoutManager =
                                                LinearLayoutManager(this@ReturnItemsActivity)
                                            orderProductsList.adapter = adapter
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Utils.dbErToast(this@ReturnItemsActivity)
                                        }
                                    }
                                )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Utils.dbErToast(this@ReturnItemsActivity)
                    }
                })
        }
    }

    inner class OrderProductItem(
        private val context: Context,
        private val snapshot: DataSnapshot,
        private val productsList: ArrayList<Product>
    ) :
        Item<GroupieViewHolder>() {
        var returnQuantity = 0
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val quantity = snapshot.child("quantity").value.toString().toInt()
            GlideApp.with(context).load(snapshot.child("productImage").value.toString())
                .placeholder(R.drawable.tea_beverages)
                .into(viewHolder.itemView.orderProductImage)
            viewHolder.itemView.productName.text = snapshot.child("productName").value.toString()
            viewHolder.itemView.productPrice.text =
                context.getString(R.string.rs) + snapshot.child("productPrice").value.toString()
            viewHolder.itemView.productQty.text = snapshot.child("quantity").value.toString()

            viewHolder.itemView.item_quantity.text = returnQuantity.toString()

            viewHolder.itemView.minusCartBtn.setOnClickListener {
                if (returnQuantity > 0) {
                    returnQuantity--
                    viewHolder.itemView.item_quantity.text = returnQuantity.toString()
                }
            }

            viewHolder.itemView.addCartBtn.setOnClickListener {
                if (returnQuantity < quantity) {
                    returnQuantity++
                    viewHolder.itemView.item_quantity.text = returnQuantity.toString()
                }
            }

            returnItems.setOnClickListener {
                ordersRef.child(sharedPreference.getUserId()!!).child(orderId!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            returnRef.child(sharedPreference.getUserId()!!)
                                .child(orderId!!)
                                .setValue(snapshot.value)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        val map = HashMap<String, Any>()
                                        map["returnQuantity"] = returnQuantity
                                        returnRef.child(sharedPreference.getUserId()!!)
                                            .child(orderId!!)
                                            .child("Products")
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    for (snap in snapshot.children) {
                                                        returnRef.child(sharedPreference.getUserId()!!)
                                                            .child(orderId!!)
                                                            .child("Products")
                                                            .child(snap.child("productId").value.toString())
                                                            .updateChildren(map)
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Utils.dbErToast(context)
                                                }

                                            })
                                    } else {
                                        Utils.toast(context, "Error")
                                    }
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Utils.dbErToast(context)
                        }
                    })
            }
        }

        override fun getLayout() = R.layout.item_completed_order_product

    }
}