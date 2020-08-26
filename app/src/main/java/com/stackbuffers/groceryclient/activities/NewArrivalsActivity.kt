package com.stackbuffers.groceryclient.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.beverages.ItemDetailsActivity
import com.stackbuffers.groceryclient.utils.GlideApp
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_arrivals.*
import kotlinx.android.synthetic.main.item_new_arrivals.view.*

class NewArrivalsActivity : AppCompatActivity() {

    private val newArrivalRef = FirebaseDatabase.getInstance().getReference("/NewArrival")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_arrivals)

        back.setOnClickListener {
            finish()
        }

        newArrivalRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newArrivalsItemsAdapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    if (it.hasChild("Product_ID"))
                        newArrivalsItemsAdapter.add(NewArrivalsItem(it))
                }

                newArrivalsList.layoutManager =
                    LinearLayoutManager(
                        this@NewArrivalsActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                newArrivalsList.adapter = newArrivalsItemsAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@NewArrivalsActivity,
                    getString(R.string.db_er),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }
}

class NewArrivalsItem(private val dataSnapshot: DataSnapshot) :
    Item<GroupieViewHolder>() {
    override fun getLayout() = R.layout.item_new_arrivals

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            with(viewHolder.itemView) {
                val productId = dataSnapshot.child("Product_ID").value.toString()
                val unit = dataSnapshot.child("Unit").value.toString()
                val productImage = dataSnapshot.child("Product_image").value.toString()
                val productName = dataSnapshot.child("Product_Name").value.toString()
                val productPrice = dataSnapshot.child("Product_Price").value.toString()
                val discountedPrice = dataSnapshot.child("Discount_Price").value.toString()

                item_quantity.text = unit
                GlideApp.with(context).load(productImage).into(image)
                name.text = productName
                if (discountedPrice == "") {
                    price.text = productPrice
                } else {
                    price.text = discountedPrice
                }
                this.setOnClickListener {
                    val intent = Intent(context, ItemDetailsActivity::class.java)
                    intent.putExtra("product_id", productId)
                    context.startActivity(intent)
                }
            }
        }
    }
}