package com.stackbuffers.groceryclient.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_return.*
import kotlinx.android.synthetic.main.item_completed_order.view.*
import java.text.SimpleDateFormat
import java.util.*

class ReturnActivity : AppCompatActivity() {

    private val ordersRef = FirebaseDatabase.getInstance().getReference("/Orders")
    private val returnRef = FirebaseDatabase.getInstance().getReference("/Return")
    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return)

        sharedPreference = SharedPreference(this@ReturnActivity)

        back.setOnClickListener {
            finish()
        }

        ordersRef
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adapter = GroupAdapter<GroupieViewHolder>()
                    for (snap in snapshot.children) {
                        val status = snap.child("status").value
                        status?.let {
                            if (it.toString().equals(
                                    "Delivered",
                                    ignoreCase = true
                                ) && snap.child("userId").value.toString() ==
                                sharedPreference.getUserId()!! &&
                                snap.child("type").value.toString()
                                    .equals("direct", ignoreCase = true)
                            ) {
                                returnRef.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (!snapshot.hasChild(snap.key.toString())) {
                                            adapter.add(OrderItem(this@ReturnActivity, snap))
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Utils.dbErToast(this@ReturnActivity)
                                    }
                                })
                            }
                        }
                    }
                    ordersList.layoutManager = LinearLayoutManager(this@ReturnActivity)
                    ordersList.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@ReturnActivity)
                }
            })
    }

    override fun onBackPressed() {
        finish()
    }

    inner class OrderItem(
        private val context: Context,
        private val snap: DataSnapshot
    ) :
        Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.orderId.text = snap.child("orderId").value.toString()
            val dateInMillis = snap.child("date").value.toString()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dateInMillis.toLong()
            val formatter = SimpleDateFormat("dd-MM-yyyy")
            viewHolder.itemView.orderDate.text = formatter.format(calendar.time)
            viewHolder.itemView.orderPrice.text = snap.child("totalPrice").value.toString()
            viewHolder.itemView.productCount.text = snap.child("Products").childrenCount.toString()

            viewHolder.itemView.setOnClickListener {
                val intent = Intent(context, ReturnItemsActivity::class.java)
                intent.putExtra("order_id", snap.child("orderId").value.toString())
                startActivity(intent)
            }
        }

        override fun getLayout() = R.layout.item_completed_order

    }
}