package com.example.nameplaceholder

import android.app.Application
import android.content.Intent
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler

class ApplicationVK: Application() {

    override fun onCreate() {
        super.onCreate()

        VK.addTokenExpiredHandler(tokenTracker)
        VK.initialize(this)
    }

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            Intent(applicationContext,MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .also { startActivity(it) }
        }
    }
}