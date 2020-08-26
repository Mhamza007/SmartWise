package com.stackbuffers.groceryclient.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.stackbuffers.groceryclient.activities.beverages.SubCategoriesActivity
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.MainActivity
import com.stackbuffers.groceryclient.activities.NewArrivalsActivity
import com.stackbuffers.groceryclient.activities.PromotionsActivity
import com.stackbuffers.groceryclient.activities.beverages.CategoriesActivity
import com.stackbuffers.groceryclient.activities.beverages.ItemDetailsActivity
import com.stackbuffers.groceryclient.activities.beverages.ProductsActivity
import com.stackbuffers.groceryclient.model.Banner
import com.stackbuffers.groceryclient.model.Category
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.MainSliderAdapter
import com.stackbuffers.groceryclient.utils.NewArrivalSliderAdapter
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.item_new_arrivals.view.*
import kotlinx.android.synthetic.main.item_promotion.view.*
import kotlinx.android.synthetic.main.item_promotion.view.image
import kotlinx.android.synthetic.main.item_promotion.view.item_quantity
import kotlinx.android.synthetic.main.item_promotion.view.name
import java.lang.Exception

class HomeFragment : Fragment() {
    private lateinit var categoriesList: RecyclerView
    private lateinit var promotionList: RecyclerView
    private lateinit var newArrivalsList: RecyclerView
    private val categoriesRef = FirebaseDatabase.getInstance().getReference("/Categories")
    private val promotionRef = FirebaseDatabase.getInstance().getReference("/Promotion")
    private val newArrivalRef = FirebaseDatabase.getInstance().getReference("/NewArrival")
    private val bannersRef = FirebaseDatabase.getInstance().getReference("/Banners")

    private lateinit var mainBannersList: ArrayList<Banner>
    private lateinit var newArrivalsBannersList: ArrayList<Banner>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        categoriesList = view.findViewById(R.id.categoriesList)
        promotionList = view.findViewById(R.id.promotionList)
        newArrivalsList = view.findViewById(R.id.newArrivalsList)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainBannersList = ArrayList()
        mainSlider.setAdapter(MainSliderAdapter(context!!))
        mainSlider.setLoopSlides(true)
        bannersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if (it.hasChild("Banner_Type") && it.child("Banner_Type").value.toString()
                            .equals("Home", ignoreCase = true)
                    ) {
                        mainBannersList.add(
                            Banner(
                                it.child("Banner_ID").value.toString(),
                                it.child("Banner_Image").value.toString(),
                                it.child("Banner_Type").value.toString(),
                                it.child("Category_ID").value.toString(),
                                it.child("Product_ID").value.toString(),
                                it.child("Sub_Categories_ID").value.toString()
                            )
                        )
                        try {
                            mainSlider.setOnSlideClickListener { position ->
                                if (mainBannersList[position].Product_ID != "") {
                                    // Open Product Details Activity
                                    val intent = Intent(context!!, ItemDetailsActivity::class.java)
                                    intent.putExtra(
                                        "product_id",
                                        mainBannersList[position].Product_ID
                                    )
                                    startActivity(intent)
                                } else if (mainBannersList[position].Category_ID != "" && mainBannersList[position].Sub_Categories_ID != "") {
                                    // Open Products Activity
                                    val intent = Intent(context!!, ProductsActivity::class.java)
                                    intent.putExtra(
                                        "category_id",
                                        mainBannersList[position].Category_ID
                                    )
                                    intent.putExtra(
                                        "sub_category_id",
                                        mainBannersList[position].Sub_Categories_ID
                                    )
                                    startActivity(intent)
                                } else {
                                    // Open Sub Category Activity
                                    val intent =
                                        Intent(context!!, SubCategoriesActivity::class.java)
                                    intent.putExtra(
                                        "category_id",
                                        mainBannersList[position].Category_ID
                                    )
                                    startActivity(intent)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(context!!)
            }

        })

        // Promotions
        promotionViewAll.setOnClickListener {
            context?.startActivity(Intent(context!!, PromotionsActivity::class.java))
        }
        fetchPromotions()

        // Categories
        allCatViewAll.setOnClickListener {
            context?.startActivity(Intent(context!!, CategoriesActivity::class.java))
        }
        fetchCategories()

        newArrivalsBannersList = ArrayList()
        newArrivalSlider.setAdapter(NewArrivalSliderAdapter(context!!))
        newArrivalSlider.setLoopSlides(true)
        bannersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if (it.hasChild("Banner_Type") && it.child("Banner_Type").value.toString()
                            .equals("NewArrival", ignoreCase = true)
                    ) {
                        newArrivalsBannersList.add(
                            Banner(
                                it.child("Banner_ID").value.toString(),
                                it.child("Banner_Image").value.toString(),
                                it.child("Banner_Type").value.toString(),
                                it.child("Category_ID").value.toString(),
                                it.child("Product_ID").value.toString(),
                                it.child("Sub_Categories_ID").value.toString()
                            )
                        )
                        try {
                            newArrivalSlider.setOnSlideClickListener { position ->
                                if (newArrivalsBannersList[position].Product_ID != "") {
                                    // Open Product Details Activity
                                    val intent = Intent(context!!, ItemDetailsActivity::class.java)
                                    intent.putExtra(
                                        "product_id",
                                        newArrivalsBannersList[position].Product_ID
                                    )
                                    startActivity(intent)
                                } else if (newArrivalsBannersList[position].Category_ID != "" && newArrivalsBannersList[position].Sub_Categories_ID != "") {
                                    // Open Products Activity
                                    val intent = Intent(context!!, ProductsActivity::class.java)
                                    intent.putExtra(
                                        "category_id",
                                        newArrivalsBannersList[position].Category_ID
                                    )
                                    intent.putExtra(
                                        "sub_category_id",
                                        newArrivalsBannersList[position].Sub_Categories_ID
                                    )
                                    startActivity(intent)
                                } else {
                                    // Open Sub Category Activity
                                    val intent =
                                        Intent(context!!, SubCategoriesActivity::class.java)
                                    intent.putExtra(
                                        "category_id",
                                        newArrivalsBannersList[position].Category_ID
                                    )
                                    startActivity(intent)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(context!!)
            }
        })

        // New Arrivals
        newArrivalsViewAll.setOnClickListener {
            context?.startActivity(Intent(context!!, NewArrivalsActivity::class.java))
        }
        fetchNewArrivals()
    }

    private fun fetchCategories() {
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
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

    private fun fetchPromotions() {
        promotionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val promotionItemsAdapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    if (it.hasChild("Product_ID"))
                        promotionItemsAdapter.add(PromotionItem(context!!, it))
                }

                promotionList.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                promotionList.adapter = promotionItemsAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.db_er), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchNewArrivals() {
        newArrivalRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newArrivalsItemsAdapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    if (it.hasChild("Product_ID"))
                        newArrivalsItemsAdapter.add(NewArrivalsItem(context!!, it))
                }

                newArrivalsList.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                newArrivalsList.adapter = newArrivalsItemsAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, getString(R.string.db_er), Toast.LENGTH_SHORT).show()
            }
        })
    }
}


class CategoryItem(private val context: Context, private val category: Category) :
    Item<GroupieViewHolder>() {
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

class PromotionItem(private val context: Context, private val dataSnapshot: DataSnapshot) :
    Item<GroupieViewHolder>() {
    override fun getLayout() = R.layout.item_promotion

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
                    oldPrice.visibility = View.GONE
                    newPrice.text = productPrice
                } else {
                    oldPrice.text = productPrice
                    newPrice.text = discountedPrice
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

class NewArrivalsItem(private val context: Context, private val dataSnapshot: DataSnapshot) :
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
