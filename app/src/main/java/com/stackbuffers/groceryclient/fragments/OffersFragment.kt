package com.stackbuffers.groceryclient.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.ItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_offers.*

class OffersFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_offers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val offersAdapter = GroupAdapter<GroupieViewHolder>()

        offersAdapter.add(OfferItem(context!!))
        offersAdapter.add(OfferItem(context!!))
        offersAdapter.add(OfferItem(context!!))
        offersAdapter.add(OfferItem(context!!))
        offersAdapter.add(OfferItem(context!!))
        offersAdapter.add(OfferItem(context!!))
        offersAdapter.add(OfferItem(context!!))
        offersAdapter.add(OfferItem(context!!))

        offersList.addItemDecoration(ItemDecoration(50))
        offersList.layoutManager = LinearLayoutManager(context)
        offersList.adapter = offersAdapter
    }
}

class OfferItem(private val context: Context) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_offer
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
    }

}