package com.seancolombo.freeair.widget

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration

private const val UNIQUE_WORK_NAME = "free_air_widget_refresh"

/** Ties periodic refresh to whether a widget is actually placed -- see FreeAirWidgetReceiver. */
object FreeAirWidgetScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<FreeAirWidgetRefreshWorker>(Duration.ofMinutes(15))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
