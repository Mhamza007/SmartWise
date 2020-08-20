package com.stackbuffers.groceryclient.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.stackbuffers.groceryclient.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_wish_list.*

class WishListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_list)

        back.setOnClickListener {
            finish()
        }

        val wishListAdapter = GroupAdapter<GroupieViewHolder>()

        wishListAdapter.add(WishListItem(this))
        wishListAdapter.add(WishListItem(this))
        wishListAdapter.add(WishListItem(this))

        wishList.layoutManager = LinearLayoutManager(this)
        wishList.adapter = wishListAdapter
    }
}

class WishListItem(private val context: Context) : Item<GroupieViewHolder>() {
    override fun getLayout() = R.layout.item_wish_list

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

    }
}