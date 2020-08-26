package com.stackbuffers.groceryclient.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stackbuffers.groceryclient.MyApp
import com.stackbuffers.groceryclient.R
import com.stackbuffers.groceryclient.activities.ShareCartActivity.Companion.TAG
import com.stackbuffers.groceryclient.model.MySingleton
import com.stackbuffers.groceryclient.utils.GlideApp
import com.stackbuffers.groceryclient.utils.SharedPreference
import com.stackbuffers.groceryclient.utils.Utils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_share_cart.*
import kotlinx.android.synthetic.main.item_user.view.*
import org.json.JSONException
import org.json.JSONObject

class ShareCartActivity : AppCompatActivity() {

    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")

    companion object {
        const val TAG = "ShareCartActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_cart)

        back.setOnClickListener {
            finish()
        }

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    if (it.hasChild("User_ID"))
                        adapter.add(UserItem(this@ShareCartActivity, it))
                }
                usersList.layoutManager = LinearLayoutManager(this@ShareCartActivity)
                usersList.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.dbErToast(this@ShareCartActivity)
            }
        })
    }
}

class UserItem(private val context: Context, private val dataSnapshot: DataSnapshot) :
    Item<GroupieViewHolder>() {

    private val usersRef = FirebaseDatabase.getInstance().getReference("/users")
    private val cartRef = FirebaseDatabase.getInstance().getReference("/Cart")
    private lateinit var sharedPreference: SharedPreference

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        sharedPreference = SharedPreference(context)

        GlideApp.with(context).load(dataSnapshot.child("profileImageUrl").value)
            .placeholder(R.drawable.profile_image).into(viewHolder.itemView.image)
        viewHolder.itemView.name.text = dataSnapshot.child("Name").value.toString()
        viewHolder.itemView.share.setOnClickListener {
            val userId = dataSnapshot.child("User_ID").value.toString()
            cartRef.child(sharedPreference.getUserId()!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cartRef.child(userId).setValue(snapshot.value)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Utils.toast(context, "Cart Shared")
                                    viewHolder.itemView.share.visibility = View.GONE

                                    usersRef.child(sharedPreference.getUserId()!!)
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val senderId =
                                                    snapshot.child("User_ID").value.toString()
                                                val senderName =
                                                    snapshot.child("Name").value.toString()

//                                                val topic = "/topics/Notifications"
                                                val topic =
                                                    dataSnapshot.child("token").value.toString()
                                                val notificationTitle =
                                                    "$senderId/$senderName/$userId"
                                                val notificationMessage = "Shared Cart with you"

                                                val notification = JSONObject()
                                                val notificationBody = JSONObject()

                                                try {
                                                    notificationBody.put("title", notificationTitle)
                                                    notificationBody.put(
                                                        "message",
                                                        notificationMessage
                                                    )

                                                    notification.put("to", topic)
                                                    notification.put("data", notificationBody)
                                                } catch (e: JSONException) {
                                                    e.printStackTrace()
                                                }
                                                sendNotification(notification)
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Utils.dbErToast(context)
                                            }
                                        })
                                } else {
                                    Utils.toast(context, "Failed to share Cart")
                                }
                            }.addOnFailureListener {
                                Utils.toast(context, "Failed to share Cart")
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Utils.dbErToast(context)
                    }
                })
        }
    }

    private fun sendNotification(notification: JSONObject) {
        val jsonObjectRequest = object : JsonObjectRequest(MyApp.FCM_API, notification,
            { response -> Log.i(TAG, "onResponse: $response") },
            { error ->
                Utils.toast(context, "Notifications Request error")
                Log.e(TAG, "Notifications Request error $error")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = MyApp.SERVER_KEY
                params["Content-Type"] = MyApp.CONTENT_TYPE
                return params
            }
        }
        MySingleton.getInstance(context.applicationContext)?.addToRequestQueue(jsonObjectRequest)
    }

    override fun getLayout() = R.layout.item_user
}