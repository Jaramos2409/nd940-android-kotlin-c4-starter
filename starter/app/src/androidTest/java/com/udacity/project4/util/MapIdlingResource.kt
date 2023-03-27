package com.udacity.project4.util

import androidx.test.espresso.IdlingResource
import com.google.android.gms.maps.SupportMapFragment

class MapIdlingResource(private val mapFragment: SupportMapFragment) : IdlingResource {
    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String {
        return "MapIdlingResource"
    }

    override fun isIdleNow(): Boolean {
        val idle = mapFragment.view != null && mapFragment.requireView().isShown
        if (idle) {
            resourceCallback?.onTransitionToIdle()
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }
}
