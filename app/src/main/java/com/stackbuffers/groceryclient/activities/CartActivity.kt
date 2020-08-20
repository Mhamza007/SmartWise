package com.stackbuffers.groceryclient.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.ItemDecoration
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.cart_item.view.*

class CartActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        sharedPreference = SharedPreference(this)

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
                                    cartItemAdapter.add(CartItem(this@CartActivity, snapshot))
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(
                                        this@CartActivity,
                                        "Database Error",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            })
                    }
                    cartItemList.addItemDecoration(ItemDecoration(50))
                    cartItemList.layoutManager = LinearLayoutManager(this@CartActivity)
                    cartItemList.adapter = cartItemAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CartActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }

        })
    }
}

class CartItem(private val context: Context, private val snapshot: DataSnapshot) :
    Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.cart_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

//        val discountedPrice = (snapshot.child("Discount_Price").value.toString()).toDouble()
//        val pricePrice = (snapshot.child("Product_Price").value.toString()).toDouble()
//        val oldPrice = discountedPrice + pricePrice

        GlideApp.with(context).load(snapshot.child("Product_image").value).into(viewHolder.itemView.image)
        viewHolder.itemView.name.text = snapshot.child("Product_Name").value.toString()
//        viewHolder.itemView.oldPrice.text = oldPrice.toString()
        viewHolder.itemView.newPrice.text = snapshot.child("Product_Price").value.toString()
    }

}