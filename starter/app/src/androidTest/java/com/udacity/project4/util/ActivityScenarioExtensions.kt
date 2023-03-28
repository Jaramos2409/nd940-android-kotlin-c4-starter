package com.udacity.project4.util

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

@JvmName("activityScenarioGetActivity")
inline fun <reified A : Activity> ActivityScenario<A>.getActivityReference(): A {
    val activityRef: AtomicReference<A> = AtomicReference()
    val future = CompletableFuture<Void>()
    onActivity { activity ->
        run {
            activityRef.set(activity)
            future.complete(null)
        }
    }
    future.get()

    return activityRef.get()
}

