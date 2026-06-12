package com.leona.controlepagamentos.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.leona.controlepagamentos.PaymentsApplication
import com.leona.controlepagamentos.domain.capture.NotificationText
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PaymentNotificationListenerService : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val application = application as? PaymentsApplication ?: return
        val packageName = sbn.packageName ?: return

        scope.launch {
            if (!application.container.settingsDataStore.settings.first().captureEnabled) return@launch

            val source = application.container.database.notificationSourceDao().getByPackage(packageName)
            if (source?.isEnabled != true) return@launch

            val extras = sbn.notification.extras
            val notificationText = NotificationText(
                title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
                text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
                subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
                bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            )

            application.container.captureProcessor.handle(
                sourcePackage = packageName,
                sourceAppName = source.appName,
                text = notificationText,
                occurredAt = Instant.ofEpochMilli(sbn.postTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            )
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
