package com.stackbuffers.groceryclient.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_wish_list.*
import kotlinx.android.synthetic.main.item_wish_list.view.*

class WishListActivity : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_list)

        sharedPreference = SharedPreference(this)

        back.setOnClickListener {
            finish()
        }

        val wishListRef = FirebaseDatabase.getInstance().getReference("/WishList")
        val productsRef = FirebaseDatabase.getInstance().getReference("/Products")

        wishListRef.child(sharedPreference.getUserId()!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val wishListAdapter = GroupAdapter<GroupieViewHolder>()

                        snapshot.children.forEach {
                            productsRef.child(it.key!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        wishListAdapter.add(
                                            WishListItem(
                                                this@WishListActivity,
                                                snapshot
                                            )
                                        )
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Utils.dbErToast(this@WishListActivity)
                                    }
                                })
                        }
                        wishList.layoutManager = LinearLayoutManager(this@WishListActivity)
                        wishList.adapter = wishListAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(this@WishListActivity)
                }
            })
    }
}

class WishListItem(private val context: Context, private val snapshot: DataSnapshot) :
    Item<GroupieViewHolder>() {

    private val rs = context.getString(R.string.rs)
    private val discountedPrice = snapshot.child("Discount_Price").value.toString()
    private val productPrice = snapshot.child("Product_Price").value.toString()

    override fun getLayout() = R.layout.item_wish_list

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        GlideApp.with(context).load(snapshot.child("Product_image").value)
            .into(viewHolder.itemView.image)
        viewHolder.itemView.name.text = snapshot.child("Product_Name").value.toString()
        if (discountedPrice == "") {
            viewHolder.itemView.price.text = rs + productPrice
        } else {
            viewHolder.itemView.price.text = rs + discountedPrice
        }
    }
}