package network.geodema.misetanibox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Автозапуск после перезагрузки:
 *  - обычный VPN-туннель, если включён автозапуск (VpnPrefs.KEY_AUTOSTART);
 *  - напоминание об истечении подписки (ExpiryReminder, апстрим);
 *  - сервис-наблюдатель за приложением (AppWatcherService, функция форка) —
 *    сам он НЕ переживает перезагрузку устройства, поэтому его нужно поднимать
 *    здесь так же, как основной туннель.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        ExpiryReminder.schedule(context)

        if (VpnPrefs.isAutostart(context)) {
            VpnPrefs.startFromPrefs(context)
        }

        if (VpnPrefs.isAppWatcherEnabled(context) &&
            VpnPrefs.appTriggerPackages(context).isNotEmpty() &&
            hasUsageAccess(context)
        ) {
            val i = Intent(context, AppWatcherService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }
    }
}
