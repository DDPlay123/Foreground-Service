package mai.project.foreground_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * 倒數計時 Service
 *
 * https://developer.android.com/about/versions/14/changes/fgs-types-required?hl=zh-tw
 */
class CountdownService : Service() {
    private val binder = CountdownBinder()
    private var countDownTimer: CountDownTimer? = null
    private var isTimerActive = false
    var timeRemaining: Long = 0
        private set

    var countdownListener: CountdownListener? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("倒數計時 Service"))
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder = binder

    private fun createNotification(contentText: String): Notification {
        val notificationChannelId = "channel_id"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Countdown Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(notificationChannelId, name, importance)
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = getDefaultPendingIntent()

        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("倒數計時 Service")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)

        return builder.build()
    }

    private fun getDefaultPendingIntent(): PendingIntent {
        val resultIntent = Intent(applicationContext, MainActivity::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )
    }

    private fun updateNotification(second: Long) {
        val notification = createNotification("剩餘時間: $second 秒")
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun isCountingDown(): Boolean = isTimerActive

    fun startCountdown(second: Long) {
        countDownTimer?.cancel() // 取消任何現有的計時器
        isTimerActive = true
        countDownTimer = object : CountDownTimer(second * ONE_SECOND, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished / ONE_SECOND
                updateNotification(timeRemaining)
                countdownListener?.onTick(timeRemaining)
            }

            override fun onFinish() {
                isTimerActive = false
                countdownListener?.onFinish()
            }
        }.start()
    }

    fun stopCountdown() {
        countDownTimer?.cancel()
        isTimerActive = false
    }

    inner class CountdownBinder : Binder() {
        fun getService(): CountdownService = this@CountdownService
    }

    interface CountdownListener {
        fun onTick(second: Long)
        fun onFinish()
    }

    companion object {
        private const val NOTIFICATION_ID = 1

        private const val ONE_SECOND = 1000L
    }
}