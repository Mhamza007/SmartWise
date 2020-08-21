package com.stackbuffers.groceryclient.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.stackbuffers.groceryclient.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_orders.*

class OrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        back.setOnClickListener {
            finish()
        }

        val ordersAdapter = GroupAdapter<GroupieViewHolder>()
        ordersAdapter.add(OrderItem())
        ordersAdapter.add(OrderItem())
        ordersAdapter.add(OrderItem())
        ordersAdapter.add(OrderItem())
        ordersAdapter.add(OrderItem())
        ordersAdapter.add(GrandTotalItem())

        orderItemsList.layoutManager = LinearLayoutManager(this)
        orderItemsList.adapter = ordersAdapter
    }
}

class OrderItem : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_order
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

    }

}

class GrandTotalItem : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_grand_total
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

    }
}