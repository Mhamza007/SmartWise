package com.stackbuffers.groceryclient.activities.orders

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
import com.stackbuffers.groceryclient.activities.OrderDetailsActivity
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_orders.*
import kotlinx.android.synthetic.main.item_completed_order.view.*
import java.text.SimpleDateFormat
import java.util.*

class MyOrdersActivity : AppCompatActivity() {

    private val ordersRef = FirebaseDatabase.getInstance().getReference("/Orders")
    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        sharedPreference = SharedPreference(this@MyOrdersActivity)

        back.setOnClickListener {
            finish()
        }

        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val myOrdersAdapter = GroupAdapter<GroupieViewHolder>()
                    for (snap in snapshot.children) {
                        if (snap.child("userId").value.toString() == sharedPreference.getUserId()!!) {
                            //show orders
                            myOrdersAdapter.add(OrderItem(this@MyOrdersActivity, snap))
                        }
                    }
                    ordersList.layoutManager = LinearLayoutManager(this@MyOrdersActivity)
                    ordersList.adapter = myOrdersAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(this@MyOrdersActivity)
            }
        })
    }
}

class OrderItem(
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
            val intent = Intent(context, OrderDetailsActivity::class.java)
            intent.putExtra("order_id", snap.child("orderId").value.toString())
            context.startActivity(intent)
        }
    }

    override fun getLayout() = R.layout.item_completed_order

}