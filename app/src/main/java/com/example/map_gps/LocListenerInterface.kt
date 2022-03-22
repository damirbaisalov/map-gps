package com.example.map_gps

import android.location.Location

interface LocListenerInterface {

    fun onLocationChanged(loc: Location)

}