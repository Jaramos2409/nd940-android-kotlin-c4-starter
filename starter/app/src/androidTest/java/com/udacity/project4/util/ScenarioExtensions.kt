package com.udacity.project4.util

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

@JvmName("activityScenarioGetActivity")
inline fun <reified A : Activity> ActivityScenario<A>.getActivityReference(): A {
    val activityRef: AtomicReference<A> = AtomicReference()
    val future = CompletableFuture<Void>()

    moveToState(Lifecycle.State.RESUMED)

    onActivity { activity ->
        run {
            activityRef.set(activity)
            future.complete(null)
        }
    }
    future.get()

    return activityRef.get()
}

@JvmName("fragmentScenarioGetActivity")
inline fun <reified F : Fragment> FragmentScenario<F>.getActivityReference(): Activity {
    val activityRef: AtomicReference<Activity> = AtomicReference()
    val future = CompletableFuture<Void>()

    moveToState(Lifecycle.State.RESUMED)

    onFragment { fragment ->
        run {
            activityRef.set(fragment.requireActivity())
            future.complete(null)
        }
    }
    future.get()

    return activityRef.get()
}

