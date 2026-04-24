package com.gastosapp.core.util

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GastosFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Manejar notificaciones push entrantes
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Enviar token al servidor si fuera necesario
    }
}
