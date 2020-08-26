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
import com.stackbuffers.groceryclient.model.Category
import com.stackbuffers.groceryclient.utils.GlideApp
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_categories.*
import kotlinx.android.synthetic.main.item_category.view.*

class CategoriesActivity : AppCompatActivity() {

    private val categoriesRef = FirebaseDatabase.getInstance().getReference("/Categories")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        back.setOnClickListener {
            finish()
        }

        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CategoriesActivity, getString(R.string.db_er), Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryItemsAdapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    val category = it.getValue(Category::class.java)
                    if (category != null) {
                        categoryItemsAdapter.add(CategoryItem(this@CategoriesActivity, category))
                    }
                    categoriesList.layoutManager = GridLayoutManager(this@CategoriesActivity, 3)
                    categoriesList.adapter = categoryItemsAdapter
                }
            }
        })
    }
}

class CategoryItem(
    private val context: Context,
    private val category: Category
) : Item<GroupieViewHolder>() {
    override fun getLayout() = R.layout.item_category

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        GlideApp.with(context).load(category.Category_image).placeholder(R.drawable.tea_beverages)
            .into(viewHolder.itemView.categoryImage)
        viewHolder.itemView.categoryName.text = category.Category_Name

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, SubCategoriesActivity::class.java)
            intent.putExtra("category_id", category.Category_ID)
            context.startActivity(intent)
        }
    }
}