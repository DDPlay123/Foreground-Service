package mai.project.foreground_service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * 倒數計時 Service
 *
 * https://developer.android.com/about/versions/14/changes/fgs-types-required?hl=zh-tw
 */
class CountdownService : Service() {
    // 倒數計時器
    private var countDownTimer: CountDownTimer? = null

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startCountdown(intent.getLongExtra(EXTRA_COUNTDOWN_TIME, 0))
                startForeground(NOTIFICATION_ID, createNotification("Foreground Service"))
            }

            ACTION_STOP -> {
                stopCountdown()
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotification(contentText: String): Notification {
        val resultIntent = Intent(applicationContext, MainActivity::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val builder = NotificationCompat.Builder(this, App.CHANNEL_ID)
            .setContentTitle("倒數計時 Service")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)

        return builder.build()
    }

    private fun updateNotification(second: Long) {
        val notification = createNotification("剩餘時間: $second 秒")
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun startCountdown(second: Long) {
        countDownTimer?.cancel() // 取消任何現有的計時器
        countDownTimer = object : CountDownTimer(second * ONE_SECOND, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                val timeRemaining = millisUntilFinished / ONE_SECOND
                SharedRepository.setCountdownTime(timeRemaining)
                updateNotification(timeRemaining)
                Log.e("CountdownService", "onTick: $timeRemaining")
            }

            override fun onFinish() {
                Log.e("CountdownService", "onFinish")
                stopSelf()
            }
        }.start()
    }

    private fun stopCountdown() {
        countDownTimer?.cancel()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val ONE_SECOND = 1000L

        const val ACTION_START = "CountdownService.action.START"
        const val ACTION_STOP = "CountdownService.action.STOP"
        const val EXTRA_COUNTDOWN_TIME = "CountdownService.extra.COUNTDOWN_TIME"
    }
}