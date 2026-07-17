package com.seancolombo.freeair.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Thin WorkManager glue: the actual fetch/cache/render logic lives in FreeAirWidget itself. */
class FreeAirWidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        FreeAirWidget().updateAll(applicationContext)
        return Result.success()
    }
}
