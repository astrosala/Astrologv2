package com.astrolog.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.astrolog.app.R
import com.astrolog.app.data.database.AstroDatabase
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────
// Worker: comprueba objetos con alerta activa y notifica
// ─────────────────────────────────────────────────────────
class VisibilityAlertWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "astrolog_alerts"
        const val WORK_TAG = "visibility_alert"

        fun schedule(context: Context) {
            // Comprueba cada día a las 20:00 si hay objetos en temporada óptima
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val delay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<VisibilityAlertWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        }

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Alertas de visibilidad AstroLog",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones cuando un objeto está en su mejor visibilidad"
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
        }
    }

    override fun doWork(): Result {
        val db = AstroDatabase.getDatabase(context)
        val objects = runBlocking { db.astroObjectDao().getObjectsWithAlerts() }
        if (objects.isEmpty()) return Result.success()

        val currentMonth = when (Calendar.getInstance().get(Calendar.MONTH) + 1) {
            3 -> "Marzo"
            4 -> "Abril"
            5 -> "Mayo"
            6 -> "Junio"
            else -> return Result.success()
        }

        val optimalObjects = objects.filter { obj ->
            val months = obj.alertMonths.split(",").map { it.trim() }
            currentMonth in months
        }

        optimalObjects.forEach { obj ->
            sendNotification(obj.name, currentMonth)
        }

        return Result.success()
    }

    private fun sendNotification(objectName: String, month: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_star)
            .setContentTitle("★ Visibilidad óptima — $month")
            .setContentText("$objectName está en su mejor momento para fotografiar")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$objectName está en visibilidad óptima en $month. ¡Buenas condiciones para salir esta noche!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(objectName.hashCode(), notification)
    }
}

// ─────────────────────────────────────────────────────────
// BroadcastReceiver: re-programa alertas al arrancar
// ─────────────────────────────────────────────────────────
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            VisibilityAlertWorker.schedule(context)
        }
    }
}
