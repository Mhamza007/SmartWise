package com.stackbuffers.groceryclient.activities.orders

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
import com.stackbuffers.groceryclient.model.ManualEntryOrder
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_order_manually.*
import kotlinx.android.synthetic.main.item_manual_entry.view.*

class OrderManuallyActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference
    private lateinit var itemList: ArrayList<ManualEntryOrder>
    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")
    private val ordersRef = FirebaseDatabase.getInstance().getReference("/Orders")

    private lateinit var userAddress: String
    private lateinit var userNumber: String
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_manually)

        sharedPreference = SharedPreference(this@OrderManuallyActivity)
        itemList = ArrayList()

        back.setOnClickListener {
            finish()
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
                    Utils.dbErToast(this@OrderManuallyActivity)
                }
            })

        itemList.clear()
        val adapter = GroupAdapter<GroupieViewHolder>()
        addItem.setOnClickListener {
            val manualItemName = itemName.text.toString().trim()
            val manualItemQty = itemQty.text.toString().trim()
            if (manualItemName.isEmpty() && manualItemQty.isEmpty()
            ) {
                itemName.error = "Empty Item Name!"
                itemQty.error = "Empty Item Quantity!"
            } else if (manualItemName.isEmpty()) {
                itemName.error = "Empty Item Name!"
            } else if (manualItemQty.isEmpty()) {
                itemQty.error = "Empty Item Quantity!"
            } else {
                itemList.add(ManualEntryOrder(manualItemName, manualItemQty))
                adapter.add(
                    ManualEntryItem(
                        this@OrderManuallyActivity,
                        itemList
                    )
                )
                itemName.setText("")
                itemQty.setText("")
            }
        }
        itemsList.layoutManager = LinearLayoutManager(this@OrderManuallyActivity)
        itemsList.adapter = adapter

        placeOrder.setOnClickListener {
            // place order
            val orderId = ordersRef.push().key.toString()
            val date = System.currentTimeMillis().toString()

            val orderMap = HashMap<String, Any>()
            orderMap["orderId"] = orderId
            orderMap["date"] = date
            orderMap["status"] = "Pending"

            orderMap["userId"] = sharedPreference.getUserId()!!
            orderMap["userName"] = userName
            orderMap["mobileNumber"] = userNumber
            orderMap["address"] = userAddress

            orderMap["type"] = "manual"

            ordersRef.child(orderId)
                .setValue(orderMap)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        ordersRef.child(orderId).child("Products").setValue(itemList)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    itemList.clear()
                                    finish()
                                    Utils.toast(this@OrderManuallyActivity, "Order Placed")
                                }
                            }.addOnFailureListener {
                                Utils.toast(this@OrderManuallyActivity, "Order Failed")
                                Log.e(TAG, "Failed ${it.message}")
                            }
                }.addOnFailureListener {
                    Utils.toast(this@OrderManuallyActivity, "Order Failed")
                    Log.e(TAG, "Failed ${it.message}")
                }
        }
    }

    companion object {
        const val TAG = "OrderManuallyActivity"
    }
}

class ManualEntryItem(
    private val context: Context,
    private val itemList: ArrayList<ManualEntryOrder>
) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.item_itemName.text = itemList[position].productName
        viewHolder.itemView.item_itemQty.text = itemList[position].quantity
    }

    override fun getLayout() = R.layout.item_manual_entry

}