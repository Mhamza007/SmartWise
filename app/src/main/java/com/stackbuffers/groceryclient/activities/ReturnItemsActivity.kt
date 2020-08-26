package com.stackbuffers.groceryclient.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    private val returningRef = FirebaseDatabase.getInstance().getReference("/Returning")
    private var orderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_items)

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
            ordersRef.child(it)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dateInMillis = snapshot.child("date").value.toString()
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = dateInMillis.toLong()
                            val formatter = SimpleDateFormat("dd-MM-yyyy")
                            date.text = formatter.format(calendar.time)
                            if (snapshot.hasChild("totalPrice"))
                                price.text = snapshot.child("totalPrice").value.toString()
                            else
                                price.text = getString(R.string.manual)
                            countProducts.text = snapshot.child("Products").childrenCount.toString()

                            ordersRef.child(orderId!!)
                                .child("Products").addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val adapter = GroupAdapter<GroupieViewHolder>()
                                            snapshot.children.forEach {
                                                adapter.add(
                                                    OrderProductItem(
                                                        this@ReturnItemsActivity,
                                                        it
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
        private val dataSnapshot: DataSnapshot
    ) :
        Item<GroupieViewHolder>() {
        var returnQuantity = 0
        private val prodList = HashMap<String, Any>()

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            Log.d("dataSnapshot", "dataSnapshot $dataSnapshot")

            ordersRef.child(orderId!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        returningRef.child(orderId!!).setValue(snapshot.value)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Utils.dbErToast(context)
                    }
                })

            val quantity = dataSnapshot.child("quantity").value.toString().toInt()
            GlideApp.with(context).load(dataSnapshot.child("productImage").value.toString())
                .placeholder(R.drawable.tea_beverages)
                .into(viewHolder.itemView.orderProductImage)
            viewHolder.itemView.productName.text =
                dataSnapshot.child("productName").value.toString()
            if (dataSnapshot.hasChild("productPrice"))
                viewHolder.itemView.productPrice.text =
                    context.getString(R.string.rs) + dataSnapshot.child("productPrice").value.toString()
            else
                viewHolder.itemView.productPrice.text = context.getString(R.string.manual)
            viewHolder.itemView.productQty.text = dataSnapshot.child("quantity").value.toString()

            viewHolder.itemView.item_quantity.text = returnQuantity.toString()

            viewHolder.itemView.minusCartBtn.setOnClickListener {
                if (returnQuantity > 0) {
                    returnQuantity--
                    updateQuantity(returnQuantity)
                    viewHolder.itemView.item_quantity.text = returnQuantity.toString()
                }
            }

            viewHolder.itemView.addCartBtn.setOnClickListener {
                if (returnQuantity < quantity) {
                    returnQuantity++
                    updateQuantity(returnQuantity)
                    viewHolder.itemView.item_quantity.text = returnQuantity.toString()
                }
            }

            returnItems.setOnClickListener {
                returningRef.child(orderId!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            returnRef
                                .child(orderId!!)
                                .setValue(snapshot.value)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        returningRef.child(orderId!!)
                                            .removeValue()
                                            .addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    Utils.toast(context, "Return Requested")
                                                    finish()
                                                } else {
                                                    Utils.toast(context, "Failed to Request Return")
                                                }
                                            }.addOnFailureListener {
                                                Utils.toast(context, "Failed to Request Return")
                                            }
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

        private fun updateQuantity(returnQuantity: Int): Boolean {
            var updated = false
            returningRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(orderId!!))
                        returningRef.child(orderId!!).child("Products")
                            .addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    snapshot.children.forEach {
                                        returningRef.child(orderId!!)
                                            .child("Products")
                                            .child(dataSnapshot.child("productId").value.toString())
                                            .child("returnQuantity")
                                            .setValue(returnQuantity)
                                            .addOnCompleteListener { update ->
                                                updated = update.isSuccessful
                                            }.addOnFailureListener {
                                                updated = false
                                            }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Utils.dbErToast(context)
                                }
                            })
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(context)
                }
            })
            return updated
        }
    }
}