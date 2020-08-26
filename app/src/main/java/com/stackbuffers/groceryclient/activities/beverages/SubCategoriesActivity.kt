package com.stackbuffers.groceryclient.activities.beverages

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.model.SubCategory
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_sub_categories.*
import kotlinx.android.synthetic.main.item_beverage_list.view.*

class SubCategoriesActivity : AppCompatActivity() {

    private var categoryId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_categories)

        back.setOnClickListener {
            finish()
        }

        try {
            categoryId = intent.getStringExtra("category_id")
        } catch (e: Exception) {
            e.printStackTrace()
        }


        val subCategoriesRef = FirebaseDatabase.getInstance().getReference("/Sub_Categories")
        subCategoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(this@SubCategoriesActivity)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val beverageListAdapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    if (it.hasChild("Category_ID")) {
                        if (it.child("Category_ID").value.toString() == categoryId) {
                            val subCategory = it.getValue(SubCategory::class.java)
                            if (subCategory != null) {
                                beverageListAdapter.add(
                                    BeverageListItem(
                                        this@SubCategoriesActivity,
                                        subCategory
                                    )
                                )
                            }
                        }
                    }
                    beveragesList.layoutManager = GridLayoutManager(this@SubCategoriesActivity, 3)
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
    override fun getLayout() = R.layout.item_beverage_list

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        GlideApp.with(context).load(subCategory.Image).placeholder(R.drawable.tea_beverages)
            .into(viewHolder.itemView.beverageImage)
        viewHolder.itemView.beverageName.text = subCategory.SubCategory_Name

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, ProductsActivity::class.java)
            intent.putExtra("category_id", subCategory.Category_ID)
            intent.putExtra("sub_category_id", subCategory.SubCategory_ID)
            context.startActivity(intent)
        }
    }

}