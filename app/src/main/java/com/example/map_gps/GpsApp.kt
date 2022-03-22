package com.example.map_gps

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class GpsApp: Application() {
    override fun onCreate() {
        MapKitFactory.setApiKey("ef1262a0-ef59-4cb4-85cc-68b4e24c9af0")
        super.onCreate()
    }
}