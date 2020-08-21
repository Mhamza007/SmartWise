package com.stackbuffers.groceryclient.activities.beverages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.model.Product
import com.stackbuffers.groceryclient.utils.GlideApp
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_beverage_item.*
import kotlinx.android.synthetic.main.item_beverage.view.*

class BeverageItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beverage_item)

        back.setOnClickListener {
            finish()
        }

        val ref = FirebaseDatabase.getInstance().getReference("/Products")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@BeverageItemActivity,
                    getString(R.string.db_er),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val beverageItemsAdapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    val product = it.getValue(Product::class.java)
                    if (product != null) {
                        beverageItemsAdapter.add(BeverageItem(this@BeverageItemActivity, product))
                    }
                    beverageItemsList.layoutManager =
                        GridLayoutManager(this@BeverageItemActivity, 3)
                    beverageItemsList.adapter = beverageItemsAdapter
                }
            }

        })
    }
}

class BeverageItem(
    private val context: Context,
    private val product: Product
) : Item<GroupieViewHolder>() {

    val rs = context.getString(R.string.rs)

    override fun getLayout() = R.layout.item_beverage

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.item_quantity.text = product.Unit
        GlideApp.with(context).load(product.Product_image).placeholder(R.drawable.tea_beverages)
            .into(viewHolder.itemView.productImage)
        viewHolder.itemView.productName.text = product.Product_Name
        if (product.Discount_Price == "")
            viewHolder.itemView.productPrice.text = rs + product.Product_Price
        else
            viewHolder.itemView.productPrice.text = rs + product.Discount_Price

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, ItemDetailsActivity::class.java)
            intent.putExtra("product_id", product.Product_ID)
            context.startActivity(intent)
        }
    }
}