package com.stackbuffers.groceryclient.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.MyApp
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.orders.ManualOrderActivity
import com.stackbuffers.groceryclient.model.MySingleton
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
import kotlinx.android.synthetic.main.item_user.*
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CartActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference
    private lateinit var cartItemAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var productsList: ArrayList<Product>
    var finalPrice: Double = 0.0
    var couponApplied = false

    private lateinit var userName: String
    private lateinit var userAddress: String
    private lateinit var userNumber: String

    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")
    val ordersRef = FirebaseDatabase.getInstance().getReference("/Orders")
    val cartRef = FirebaseDatabase.getInstance().getReference("/Cart")
    val productsRef = FirebaseDatabase.getInstance().getReference("/Products")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        sharedPreference = SharedPreference(this)
        productsList = ArrayList()

        couponApplied = sharedPreference.getCouponDiscount() > 0F

        back.setOnClickListener {
            finish()
        }

        manualOrder.setOnClickListener {
            startActivity(Intent(this@CartActivity, ManualOrderActivity::class.java))
        }

        usersRef.child(sharedPreference.getUserId()!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userName = snapshot.child("Name").value.toString()
                        userAddress = snapshot.child("Address").value.toString()
                        userNumber = snapshot.child("Mobile_Number").value.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@CartActivity)
                }
            })

        cartRef.child(sharedPreference.getUserId()!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    cartItemAdapter = GroupAdapter<GroupieViewHolder>()

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
                                    if (couponApplied) {
                                        val num = finalPrice.toFloat()
                                        val value =
                                            num - (num * (sharedPreference.getCouponDiscount() / 10))
                                        checkoutPrice.text = value.toString()
                                    } else {
                                        checkoutPrice.text = finalPrice.toString()
                                    }

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
    }

    private fun reloadList() {
        val intent = Intent(this@CartActivity, CartActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
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

        private val credentialRef = FirebaseDatabase.getInstance().getReference("/Credential")
        private val rs = context.getString(R.string.rs)
        private val productPrice = snapshot.child("Product_Price").value.toString()
        private val discountedPrice = snapshot.child("Discount_Price").value.toString()
        private var quantity: Int = 1

        override fun getLayout(): Int {
            return R.layout.item_cart
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            for (i in 0..position) {
                cartRef.child(sharedPreference.getUserId()!!)
                    .child(productsList[i].Product_ID)
                    .child("quantity").setValue(1)
            }

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
                            if (couponApplied) {
                                val num = finalPrice.toFloat()
                                val value =
                                    num - (num * (sharedPreference.getCouponDiscount() / 10))
                                checkoutPrice.text = value.toString()
                            } else {
                                checkoutPrice.text = finalPrice.toString()
                            }
                        } else
                            Utils.toast(context, "Error")
                    }.addOnFailureListener {
                        Utils.toast(context, "Error")
                    }
            }

            viewHolder.itemView.minusCartBtn.setOnClickListener {
                if (quantity > 1) {
                    quantity--
                    viewHolder.itemView.item_quantity.text = quantity.toString()

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
                                Log.d(TAG, "Coupon applied : $couponApplied")
                                Log.d(TAG, "Coupon applied : ${sharedPreference.getCouponDiscount()}")
                                if (couponApplied) {
                                    val num = finalPrice.toFloat()
                                    val value =
                                        num - (num * (sharedPreference.getCouponDiscount() / 10))
                                    checkoutPrice.text = value.toString()
                                } else {
                                    checkoutPrice.text = finalPrice.toString()
                                }
                            } else
                                Utils.toast(context, "Error")
                        }.addOnFailureListener {
                            Utils.toast(context, "Error")
                        }

                }
            }

            viewHolder.itemView.close.setOnClickListener {
                cartRef.child(sharedPreference.getUserId()!!)
                    .child(productsList[position].Product_ID)
                    .removeValue()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {

                            reloadList()

                            subTotal.text = finalPrice.toString()
                            if (couponApplied) {
                                val num = finalPrice.toFloat()
                                val value =
                                    num - (num * (sharedPreference.getCouponDiscount() / 10))
                                checkoutPrice.text = value.toString()
                            } else {
                                checkoutPrice.text = finalPrice.toString()
                            }
                        }
                    }.addOnFailureListener {
                        Utils.toast(context, "Error")
                    }
            }

            checkoutLayout.setOnClickListener {
                val orderId = ordersRef.push().key!!
                val date = System.currentTimeMillis().toString()
                val map = HashMap<String, Any>()
                val totalCheckoutPrice : String
                totalCheckoutPrice = if (couponApplied) {
                    val num = finalPrice.toFloat()
                    val value =
                        num - (num * (sharedPreference.getCouponDiscount() / 10))
                    value.toString()
                } else {
                    finalPrice.toString()
                }
                cartRef.child(sharedPreference.getUserId()!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(cartSnapshot: DataSnapshot) {

                            cartSnapshot.children.forEach {

                                ordersRef
                                    .child(orderId)
                                    .child("Products")
                                    .setValue(cartSnapshot.value)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            map["orderId"] = orderId
                                            map["date"] = date
                                            map["status"] = "Pending"
                                            map["totalPrice"] = totalCheckoutPrice

                                            map["userId"] = sharedPreference.getUserId()!!
                                            map["userName"] = userName
                                            map["mobileNumber"] = userNumber
                                            map["address"] = userAddress

                                            map["type"] = "direct"

                                            ordersRef
                                                .child(orderId)
                                                .updateChildren(map)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        cartRef.child(sharedPreference.getUserId()!!)
                                                            .removeValue()
                                                        usersRef.child(sharedPreference.getUserId()!!)
                                                            .addListenerForSingleValueEvent(object :
                                                                ValueEventListener {
                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    if (snapshot.hasChild("orderCount")) {
                                                                        val orderCount =
                                                                            snapshot.child("orderCount").value.toString()
                                                                                .toInt()
                                                                        val username = snapshot.child("Name").value.toString()
                                                                        val oc = orderCount + 1
                                                                        usersRef.child(
                                                                            sharedPreference.getUserId()!!
                                                                        )
                                                                            .child("orderCount")
                                                                            .setValue(oc)
                                                                            .addOnCompleteListener {
                                                                                if (it.isSuccessful) {
                                                                                    Utils.toast(
                                                                                        this@CartActivity,
                                                                                        "Order Placed"
                                                                                    )

                                                                                    credentialRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            val topic = snapshot.child("Token").value.toString()
                                                                                            val notificationTitle = "${sharedPreference.getUserId()}/$userName"
                                                                                            val notificationMessage = "Ordered"

                                                                                            val notification = JSONObject()
                                                                                            val notificationBody = JSONObject()

                                                                                            try {
                                                                                                notificationBody.put("title", notificationTitle)
                                                                                                notificationBody.put(
                                                                                                    "message",
                                                                                                    notificationMessage
                                                                                                )

                                                                                                notification.put("to", topic)
                                                                                                notification.put("data", notificationBody)
                                                                                            } catch (e: JSONException) {
                                                                                                e.printStackTrace()
                                                                                            }
                                                                                            sendNotification(notification)
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                            Utils.dbErToast(context)
                                                                                        }

                                                                                    })

                                                                                    finish()
                                                                                } else
                                                                                    Utils.toast(
                                                                                        context,
                                                                                        "Failed to order"
                                                                                    )
                                                                            }
                                                                    } else {
                                                                        usersRef.child(
                                                                            sharedPreference.getUserId()!!
                                                                        )
                                                                            .child("orderCount")
                                                                            .setValue(1)
                                                                            .addOnCompleteListener {
                                                                                if (it.isSuccessful) {
                                                                                    Utils.toast(
                                                                                        this@CartActivity,
                                                                                        "Order Placed"
                                                                                    )
                                                                                    credentialRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                                        override fun onDataChange(
                                                                                            snapshot: DataSnapshot
                                                                                        ) {
                                                                                            val topic = snapshot.child("Token").value.toString()
                                                                                            val notificationTitle = "${sharedPreference.getUserId()}/$userName"
                                                                                            val notificationMessage = "Ordered"

                                                                                            val notification = JSONObject()
                                                                                            val notificationBody = JSONObject()

                                                                                            try {
                                                                                                notificationBody.put("title", notificationTitle)
                                                                                                notificationBody.put(
                                                                                                    "message",
                                                                                                    notificationMessage
                                                                                                )

                                                                                                notification.put("to", topic)
                                                                                                notification.put("data", notificationBody)
                                                                                            } catch (e: JSONException) {
                                                                                                e.printStackTrace()
                                                                                            }
                                                                                            sendNotification(notification)
                                                                                        }

                                                                                        override fun onCancelled(
                                                                                            error: DatabaseError
                                                                                        ) {
                                                                                            Utils.dbErToast(context)
                                                                                        }

                                                                                    })
                                                                                    finish()
                                                                                } else {
                                                                                    Utils.toast(
                                                                                        context,
                                                                                        "Failed to order"
                                                                                    )
                                                                                }
                                                                            }
                                                                    }

                                                                }

                                                                override fun onCancelled(error: DatabaseError) {
                                                                    Utils.dbErToast(context)
                                                                }
                                                            })
                                                    } else {
                                                        Utils.toast(
                                                            this@CartActivity,
                                                            "Failed to Order"
                                                        )
                                                    }
                                                }.addOnFailureListener {
                                                    Utils.toast(
                                                        this@CartActivity,
                                                        "Failed to Order"
                                                    )
                                                }
                                        } else {
                                            Utils.toast(this@CartActivity, "Failed to Order")
                                        }
                                    }.addOnFailureListener {
                                        Utils.toast(this@CartActivity, "Failed to Order")
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Utils.dbErToast(context)
                        }
                    })
            }
        }

        private fun sendNotification(notification: JSONObject) {
            val jsonObjectRequest = object : JsonObjectRequest(
                MyApp.FCM_API, notification,
                { response -> Log.i(ShareCartActivity.TAG, "onResponse: $response") },
                { error ->
                    Utils.toast(context, "Notifications Request error")
                    Log.e(ShareCartActivity.TAG, "Notifications Request error $error")
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = MyApp.SERVER_KEY
                    params["Content-Type"] = MyApp.CONTENT_TYPE
                    return params
                }
            }
            MySingleton.getInstance(context.applicationContext)?.addToRequestQueue(jsonObjectRequest)
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
