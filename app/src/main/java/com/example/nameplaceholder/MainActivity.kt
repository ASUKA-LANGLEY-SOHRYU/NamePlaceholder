package com.example.nameplaceholder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squareup.picasso.Picasso
import org.json.JSONObject
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException
import com.vk.api.sdk.requests.VKRequest

class MainActivity : AppCompatActivity() {
    lateinit var loginButton: Button
    lateinit var listView: ListView
    lateinit var mainLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginButton = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            VK.login(this, arrayListOf(VKScope.FRIENDS, VKScope.WALL, VKScope.PHOTOS))
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object: VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                var vkRequest = VKRequest<JSONObject>("account.getProfileInfo")

                //mainLayout.visibility = View.VISIBLE
                loginButton.visibility = View.GONE

                VK.execute(vkRequest, object:  VKApiCallback<JSONObject>{
                    override fun success(result: JSONObject) {
                        val name = findViewById<TextView>(R.id.fullName)
                        val response = result.getJSONObject("response")

                        val fullname = "${response["first_name"]} ${response["last_name"]}"

                        name.text = fullname
                    }
                    override fun fail(error: Exception) {
                        Toast.makeText(applicationContext,"Error: ${error.message}",Toast.LENGTH_SHORT).show()
                    }
                })

                vkRequest = VKRequest("users.get")
                vkRequest.addParam("fields","photo_400_orig")
                VK.execute(vkRequest, object: VKApiCallback<JSONObject>{
                    override fun success(result: JSONObject) {
                        val response = result.getJSONArray("response")
                        val imageView = findViewById<ImageView>(R.id.imageView)
                        val imageUri = response.getJSONObject(0).getString("photo_400_orig")
                        Picasso.with(imageView.context).load(imageUri).into(imageView)
                    }

                    override fun fail(error: Exception) {
                        Log.d("myTag","${error.message}")
                    }
                })

                vkRequest = VKRequest("friends.get")
                vkRequest.addParam("fields","nickname, photo_200_orig")
                VK.execute(vkRequest, object : VKApiCallback<JSONObject>{
                    override fun success(result: JSONObject) {
                        val textViewFriendsCount = findViewById<TextView>(R.id.friendsCount)

                        val response = result.getJSONObject("response")
                        val friendsCount: Int = response.getInt("count")
                        val items = response.getJSONArray("items")

                        val friendsList = ArrayList<VkFriend>()
                        for (i in 0 until items.length()){
                            val item = items.getJSONObject(i)
                            val friendName = item.getString("first_name")
                            val friendSurname = item.getString("last_name")
                            val friendThumbnailUrl = item.getString("photo_200_orig")

                            var vkFriend = VkFriend("$friendName $friendSurname", friendThumbnailUrl)
                            friendsList.add(vkFriend)
                        }
                        listView = findViewById(R.id.lvFriends)
                        listView.adapter = Adapter(friendsList)

                        textViewFriendsCount.text = "${getString(R.string.countOfFriends)}: $friendsCount"

                    }
                    override fun fail(error: Exception) {
                        Log.d("myTag","${error.message}")
                    }



                })


            }

            override fun onLoginFailed(authException: VKAuthException) {
                Toast.makeText(applicationContext,"Error: ${authException.authError}",Toast.LENGTH_SHORT).show()
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}

class Adapter(val items: ArrayList<VkFriend>): BaseAdapter(){
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(p0: Int): Any {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater = LayoutInflater.from(p2!!.context)

        val view = p1 ?: inflater.inflate(R.layout.friend_item, p2, false)
        val textViewFriendName = view.findViewById<TextView>(R.id.textViewFriend)
        val imageViewThumbnail = view.findViewById<ImageView>(R.id.imageViewFriend)

        val item = getItem(p0) as VkFriend

        textViewFriendName.text = item.name
        Picasso.with(imageViewThumbnail.context).load(item.thumbnailUrl).into(imageViewThumbnail)
        return view
    }

}

data class VkFriend(
    var name:String = "",
    var thumbnailUrl: String = ""
)
