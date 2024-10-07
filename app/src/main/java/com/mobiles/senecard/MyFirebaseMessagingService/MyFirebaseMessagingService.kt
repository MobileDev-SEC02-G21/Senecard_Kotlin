package com.mobiles.senecard

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMessagingService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Verifica si el mensaje tiene datos o notificación
        //remoteMessage.notification?.let {
          //  showNotification(it.title, it.body)
        //}
        super.onMessageReceived(remoteMessage)
        showNotification(remoteMessage)

    }

    // Método para mostrar la notificación
    private fun showNotification( remoteMessage: RemoteMessage) {
        val notificationManager= getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, MyAPP.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setSmallIcon(R.drawable.notification_logo)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(1,notification)

    }

    /*override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Aquí puedes manejar el nuevo token y enviarlo a tu backend si es necesario
        println("FCM Token: $token")

        // Opcional: puedes mostrar el token en un Toast o en el log
        val msg = getString(R.string.msg_token_fmt, token)
        Log.d(TAG, msg)
        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun getRegistrationToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Obtener el nuevo token de registro FCM
            val token = task.result

            // Log y Toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        }
    }
    */
     //
}
