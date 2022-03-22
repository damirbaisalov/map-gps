package com.example.map_gps

import android.location.Location
import android.location.LocationListener
import android.os.Bundle


class MyLocListener(
    private var locListenerInterface: LocListenerInterface
): LocationListener {


    override fun onLocationChanged(location: Location) {
        locListenerInterface.onLocationChanged(location)
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}