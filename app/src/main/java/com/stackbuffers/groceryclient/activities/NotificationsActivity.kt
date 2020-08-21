package com.stackbuffers.groceryclient.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.utils.ItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_notifications.*

class NotificationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        close.setOnClickListener {
            finish()
        }

        val todayNotificationsAdapter = GroupAdapter<GroupieViewHolder>()

        todayNotificationsAdapter.add(NotificationItem())

        todayNotifications.layoutManager = LinearLayoutManager(this)
        todayNotifications.adapter = todayNotificationsAdapter

        val yesterdayNotificationsAdapter = GroupAdapter<GroupieViewHolder>()

        yesterdayNotificationsAdapter.add(NotificationItem())

        yesterdayNotifications.layoutManager = LinearLayoutManager(this)
        yesterdayNotifications.adapter = yesterdayNotificationsAdapter

        val lastWeekNotificationsAdapter = GroupAdapter<GroupieViewHolder>()

        lastWeekNotificationsAdapter.add(NotificationItem())
        lastWeekNotificationsAdapter.add(NotificationItem())

        lastWeekNotifications.layoutManager = LinearLayoutManager(this)
        lastWeekNotifications.addItemDecoration(ItemDecoration(50))
        lastWeekNotifications.adapter = lastWeekNotificationsAdapter

    }
}

class NotificationItem : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.item_notification
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

    }

}