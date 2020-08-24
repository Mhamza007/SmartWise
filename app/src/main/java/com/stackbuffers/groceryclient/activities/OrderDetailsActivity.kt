package com.stackbuffers.groceryclient.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_order_details.*
import kotlinx.android.synthetic.main.item_grand_total.view.*
import kotlinx.android.synthetic.main.item_order.view.*
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference
    val ordersRef = FirebaseDatabase.getInstance().getReference("/Orders")
    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")

    private var orderId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        sharedPreference = SharedPreference(this@OrderDetailsActivity)

        back.setOnClickListener {
            finish()
        }

        try {
            orderId = intent.getStringExtra("order_id")
            order_id.text = orderId
        } catch (e: Exception) {
            e.printStackTrace()
        }

        usersRef.child(sharedPreference.getUserId()!!).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    address.text = snapshot.child("Address").value.toString()
                    phoneNumber.text = snapshot.child("Mobile_Number").value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@OrderDetailsActivity)
                }
            }
        )

        orderId?.let {
            ordersRef
                .child(it)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val dateInMillis = snapshot.child("date").value.toString()
                            Log.d(TAG, "Date $dateInMillis")
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = dateInMillis.toLong()
                            val formatter = SimpleDateFormat("dd-MM-yyyy")
                            date.text = formatter.format(calendar.time)
                            status.text = snapshot.child("status").value.toString()

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Utils.dbErToast(this@OrderDetailsActivity)
                        }
                    }
                )
        }

        // Products in order
        orderId?.let {
            ordersRef
                .child(it)
                .child("Products")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val ordersAdapter = GroupAdapter<GroupieViewHolder>()
                            for (snap in snapshot.children) {
                                ordersAdapter.add(OrderItem(snap))
                            }
                            ordersAdapter.add(GrandTotalItem())

                            orderItemsList.layoutManager =
                                LinearLayoutManager(this@OrderDetailsActivity)
                            orderItemsList.adapter = ordersAdapter
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Utils.dbErToast(this@OrderDetailsActivity)
                    }
                })
        }
    }

    class OrderItem(
        private val snapshot: DataSnapshot
    ) : Item<GroupieViewHolder>() {
        override fun getLayout() = R.layout.item_order

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.itemName.text = snapshot.child("productName").value.toString()
            viewHolder.itemView.itemQty.text = snapshot.child("quantity").value.toString()
            viewHolder.itemView.itemPrice.text = snapshot.child("productPrice").value.toString()
        }
    }

    inner class GrandTotalItem : Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.item_grand_total
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            ordersRef
//                .child(sharedPreference.getUserId()!!)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (snap in snapshot.children) {
                                viewHolder.itemView.totalPrice.text =
                                    snap.child("totalPrice").value.toString()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Utils.dbErToast(this@OrderDetailsActivity)
                        }
                    })

        }
    }

    companion object {
        const val TAG = "OrdersActivity"
    }
}