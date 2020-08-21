package com.stackbuffers.groceryclient.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.activities.beverages.BeveragesActivity
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.model.Category
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.MainSliderAdapter
import com.stackbuffers.groceryclient.utils.NewArrivalSliderAdapter
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    private lateinit var categoriesList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        categoriesList = view.findViewById(R.id.categoriesList)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainSlider.setAdapter(MainSliderAdapter())

        // Categories
        fetchCategories()

        val recommendedItemsAdapter = GroupAdapter<GroupieViewHolder>()

        recommendedItemsAdapter.add(RecommendedItem(context!!))
        recommendedItemsAdapter.add(RecommendedItem(context!!))
        recommendedItemsAdapter.add(RecommendedItem(context!!))

        promotionList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)
        promotionList.adapter = recommendedItemsAdapter

        newArrivalSlider.setAdapter(NewArrivalSliderAdapter())

        val newArrivalsItemsAdapter = GroupAdapter<GroupieViewHolder>()

        newArrivalsItemsAdapter.add(NewArrivalsItem(context!!))
        newArrivalsItemsAdapter.add(NewArrivalsItem(context!!))
        newArrivalsItemsAdapter.add(NewArrivalsItem(context!!))

        newArrivalsList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)
        newArrivalsList.adapter = newArrivalsItemsAdapter
    }

    private fun fetchCategories() {
        val ref = FirebaseDatabase.getInstance().getReference("/Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.db_er), Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryItemsAdapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    val category = it.getValue(Category::class.java)
                    if (category != null) {
                        categoryItemsAdapter.add(CategoryItem(context!!, category))
                    }
                    categoriesList.layoutManager = GridLayoutManager(context, 3)
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
            val intent = Intent(context, BeveragesActivity::class.java)
            intent.putExtra("category_id", category.Category_ID)
            context.startActivity(intent)
        }
    }

}

class RecommendedItem(private val context: Context) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_recommended
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
    }
}

class NewArrivalsItem(private val context: Context) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_new_arrivals
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
    }
}
