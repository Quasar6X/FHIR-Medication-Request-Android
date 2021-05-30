package com.example.fhir_medication_request.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.fhir_medication_request.R
import com.example.fhir_medication_request.ui.LoginActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogoutJob : JobService() {
    private val notificationHelper by lazy { NotificationHelper(applicationContext) }
    private val auth by lazy { Firebase.auth }

    override fun onStartJob(params: JobParameters?): Boolean {
        if (auth.currentUser != null) {
            auth.signOut()
            notificationHelper.send("We have logged you out automatically ノಠ益ಠノ彡┻━┻")
        }
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean = true

    class NotificationHelper(private val context: Context) {
        private val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        init {
            val channel = NotificationChannel(CHANNEL_ID, "Medication Request", NotificationManager.IMPORTANCE_HIGH)
            channel.enableLights(true)
            channel.lightColor = Color.MAGENTA
            channel.enableVibration(true)
            channel.description = "Notifications from Medication Request application"
            notifyManager.createNotificationChannel(channel)
        }

        fun send(message: String): Int {
            val id = notificationID
            val pendingIntent = PendingIntent.getActivity(context, id, Intent(context, LoginActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Medication Request Application")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_baseline_local_hospital_24)
                .setContentIntent(pendingIntent)

            notifyManager.notify(id, builder.build())

            return id
        }

        fun cancel(id: Int) = notifyManager.cancel(id)

        companion object {
            const val CHANNEL_ID = "medication_requests_default_channel"
            @JvmStatic var notificationID = 0
                get() = field++
                private set
        }
    }
}