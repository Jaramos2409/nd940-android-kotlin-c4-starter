package com.udacity.project4.utils

import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }

}

suspend inline fun <T> wrapEspressoIdlingResource(
    coroutineContext: CoroutineContext,
    crossinline function: suspend () -> T
): T {
    EspressoIdlingResource.increment() // Set app as busy.
    return try {
        withContext(coroutineContext) {
            function()
        }
    } finally {
        EspressoIdlingResource.decrement() // Set app as idle.
    }
}

