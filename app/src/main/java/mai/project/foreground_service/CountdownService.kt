package mai.project.foreground_service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
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
    private val countdownTimers = mutableMapOf<Int, CountDownTimer>()
    private var nextTimerId = 0

    override fun onDestroy() {
        super.onDestroy()
        countdownTimers.values.forEach { it.cancel() }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val timerId = nextTimerId++
                val time = intent.getLongExtra(EXTRA_COUNTDOWN_TIME, 0)
                startCountdown(time, timerId)
                startForeground(NOTIFICATION_ID, createNotification())
            }

            ACTION_STOP -> {
                val timerId = intent.getIntExtra(EXTRA_TIMER_ID, -1)
                if (timerId != -1) {
                    stopCountdown(timerId)
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotification(): Notification {
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
            .setContentText("倒數計時進行中...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)

        return builder.build()
    }

    private fun startCountdown(seconds: Long, timerId: Int) {
        countdownTimers[timerId]?.cancel()
        val timer = object : CountDownTimer(seconds * ONE_SECOND, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                val timeRemaining = millisUntilFinished / ONE_SECOND
                SharedRepository.setCountdownTime(timerId, timeRemaining)
                Log.e("CountdownService", "onTick ${timerId}: $timeRemaining")
            }

            override fun onFinish() {
                Log.e("CountdownService", "onFinish $timerId")
                countdownTimers[timerId]?.cancel()
                countdownTimers.remove(timerId)
                if (countdownTimers.isEmpty()) {
                    stopSelf()
                }
            }
        }.start()
        countdownTimers[timerId] = timer
        timer.start()
    }

    private fun stopCountdown(timerId: Int) {
        countdownTimers[timerId]?.cancel()
        countdownTimers.remove(timerId)
        if (countdownTimers.isEmpty()) {
            stopSelf()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val ONE_SECOND = 1000L

        const val ACTION_START = "CountdownService.action.START"
        const val ACTION_STOP = "CountdownService.action.STOP"
        const val EXTRA_COUNTDOWN_TIME = "CountdownService.extra.COUNTDOWN_TIME"
        const val EXTRA_TIMER_ID = "CountdownService.extra.TIMER_ID"
    }
}