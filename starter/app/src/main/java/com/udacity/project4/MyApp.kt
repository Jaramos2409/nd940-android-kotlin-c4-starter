package com.udacity.project4

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.authentication.AuthenticationRepository
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.location.CheckLocationManager
import com.udacity.project4.location.CheckLocationManagerInterface
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.geofence.GeofenceManager
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.NotificationConstants.REMINDER_NOTIFICATION_CHANNEL_ID
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import timber.log.Timber.Forest.plant


class MyApp : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            single<AuthenticationDataSource> { AuthenticationRepository(get()) }
            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(this@MyApp) }
            single {
                GeofenceManager(
                    get(),
                    get()
                )
            }
            factory<CheckLocationManagerInterface> { (context: Context, requestDeviceLocationLauncher: ActivityResultLauncher<IntentSenderRequest>) ->
                CheckLocationManager(context, requestDeviceLocationLauncher)
            }
            single {
                SaveReminderViewModel(
                    get(),
                    get()
                )
            }
            viewModel {
                AuthenticationViewModel(get())
            }
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get()
                )
            }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }

        createNotificationChannels()
    }


    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (notificationManager.getNotificationChannel(REMINDER_NOTIFICATION_CHANNEL_ID) == null) {
                val channelId = REMINDER_NOTIFICATION_CHANNEL_ID
                val channelName = getString(R.string.app_name)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(channelId, channelName, importance)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

}