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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.beverages.ItemDetailsActivity
import com.stackbuffers.groceryclient.activities.beverages.ProductsActivity
import com.stackbuffers.groceryclient.activities.beverages.SubCategoriesActivity
import com.stackbuffers.groceryclient.activities.signin.SignInActivity
import com.stackbuffers.groceryclient.activities.signup.AddPhoneNumberActivity
import com.stackbuffers.groceryclient.model.Banner
import com.stackbuffers.groceryclient.model.Cart
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.OffersSliderAdapter
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_offers.*
import kotlinx.android.synthetic.main.item_offer.view.*
import java.lang.Exception

class PromotionsFragment : Fragment() {

    private val promotionRef = FirebaseDatabase.getInstance().getReference("/Promotion")
    private val bannersRef = FirebaseDatabase.getInstance().getReference("/Banners")
    private lateinit var offersList: RecyclerView
    private lateinit var mainBannersList: ArrayList<Banner>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_offers, container, false)

        offersList = view.findViewById(R.id.offersList)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainBannersList = ArrayList()
        offersSlider.setAdapter(OffersSliderAdapter(context!!))
        offersSlider.setLoopSlides(true)
        bannersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if (it.hasChild("Banner_Type") && it.child("Banner_Type").value.toString()
                            .equals("Promotion", ignoreCase = true)
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
                            offersSlider.setOnSlideClickListener { position ->
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


        promotionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offersAdapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    if (it.hasChild("Product_ID")) {
                        offersAdapter.add(OfferItem(context!!, it))
                    }
                }

                offersList.layoutManager = LinearLayoutManager(context)
                offersList.adapter = offersAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(context!!)
            }
        })
    }
}

class OfferItem(private val context: Context, private val dataSnapshot: DataSnapshot) :
    Item<GroupieViewHolder>() {
    private val auth = FirebaseAuth.getInstance()
    private val sharedPreference = SharedPreference(context)
    private val cartRef = FirebaseDatabase.getInstance().getReference("/Cart")

    override fun getLayout() = R.layout.item_offer

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            with(viewHolder.itemView) {
                val productId = dataSnapshot.child("Product_ID").value.toString()
                val productImage = dataSnapshot.child("Product_image").value.toString()
                val productName = dataSnapshot.child("Product_Name").value.toString()
                val productPrice = dataSnapshot.child("Product_Price").value.toString()
                val discountedPrice = dataSnapshot.child("Discount_Price").value.toString()

                GlideApp.with(context).load(productImage).into(image)
                name.text = productName
                if (discountedPrice == "") {
                    oldPrice.visibility = View.GONE
                    newPrice.text = productPrice
                } else {
                    oldPrice.text = productPrice
                    newPrice.text = discountedPrice
                }
                addToCartBtn.setOnClickListener {
                    checkIfUserExists()
                }
                this.setOnClickListener {
                    val intent = Intent(context, ItemDetailsActivity::class.java)
                    intent.putExtra("product_id", productId)
                    context.startActivity(intent)
                }
            }
        }
    }

    private fun checkIfUserExists() {
        if (auth.currentUser != null) {
            val userRef =
                FirebaseDatabase.getInstance().getReference("/users")
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val number =
                        snapshot.child(sharedPreference.getUserId()!!)
                            .child("Mobile_Number").value.toString()
                    Log.d(ItemDetailsActivity.TAG, "Phone Number is $number")
                    if (number != "") {
                        // user is signed in with mobile number
                        addProductToCart()
                    } else {
                        Toast.makeText(
                            context,
                            "Mobile Number is required for adding product to cart",
                            Toast.LENGTH_SHORT
                        ).show()
                        context.startActivity(
                            Intent(
                                context,
                                AddPhoneNumberActivity::class.java
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.dbErToast(context)
                }

            })
        } else {
            // user is not signed in
            Utils.toast(context, "Sign in or Sign up to continue")
            context.startActivity(Intent(context, SignInActivity::class.java))
        }
    }

    private fun addProductToCart() {
        val productId = dataSnapshot.child("Product_ID").value.toString()
        val productImage = dataSnapshot.child("Product_image").value.toString()
        val productName = dataSnapshot.child("Product_Name").value.toString()
        val productPrice = dataSnapshot.child("Product_Price").value.toString()
        val discountedPrice = dataSnapshot.child("Discount_Price").value.toString()
        val time = "${System.currentTimeMillis()}"

        val price: String
        price = if (discountedPrice == "") productPrice else discountedPrice

        cartRef.child(sharedPreference.getUserId()!!)
            .child(productId)
            .setValue(
                Cart(
                    productId,
                    time,
                    productName,
                    productImage,
                    price,
                    1
                )
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    Utils.toast(context, "$productName Added to cart")
                } else {
                    Utils.toast(context, "Can't add product to cart, Try Again")
                }
            }.addOnFailureListener {
                Utils.toast(context, "Can't add product to cart, Try Again")
            }
    }
}