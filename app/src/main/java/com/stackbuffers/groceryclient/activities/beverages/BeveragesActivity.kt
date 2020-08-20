package com.stackbuffers.groceryclient.activities.beverages

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.model.SubCategory
import com.stackbuffers.groceryclient.utils.GlideApp
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_beverages.*
import kotlinx.android.synthetic.main.beverage_list_item.view.*

class BeveragesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beverages)

        back.setOnClickListener {
            finish()
        }


        val ref = FirebaseDatabase.getInstance().getReference("/Sub_Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@BeveragesActivity,
                    getString(R.string.db_er),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val beverageListAdapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    val subCategory = it.getValue(SubCategory::class.java)
                    if (subCategory != null) {
                        beverageListAdapter.add(
                            BeverageListItem(
                                this@BeveragesActivity,
                                subCategory
                            )
                        )
                    }
                    beveragesList.layoutManager = GridLayoutManager(this@BeveragesActivity, 3)
                    beveragesList.adapter = beverageListAdapter
                }
            }

        })
    }
}

class BeverageListItem(
    private val context: Context,
    private val subCategory: SubCategory
) : Item<GroupieViewHolder>() {
    override fun getLayout() = R.layout.beverage_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        GlideApp.with(context).load(subCategory.Image).placeholder(R.drawable.tea_beverages)
            .into(viewHolder.itemView.beverageImage)
        viewHolder.itemView.beverageName.text = subCategory.SubCategory_Name

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, BeverageItemActivity::class.java)
            intent.putExtra("category_id", subCategory.Category_ID)
            intent.putExtra("sub_category_id", subCategory.SubCategory_ID)
            context.startActivity(intent)
        }
    }

}