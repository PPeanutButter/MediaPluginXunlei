package com.peanut.xunleivpn

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SettingManager.init(this)
    }

}