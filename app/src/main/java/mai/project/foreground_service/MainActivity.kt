package mai.project.foreground_service

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import mai.project.foreground_service.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var countdownService: CountdownService
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as CountdownService.CountdownBinder
            countdownService = binder.getService()
            isBound = true
            countdownService.countdownListener = object : CountdownService.CountdownListener {
                override fun onTick(second: Long) {
                    binding.tvState.text = String.format("倒數中: %d 秒", second)
                }

                override fun onFinish() {
                    binding.tvState.text = "完成"
                    binding.btnStart.text = "開始"
                }
            }
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this@MainActivity, CountdownService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    private fun setListener() = with(binding) {
        btnStart.setOnClickListener {
            if (isBound) {
                if (countdownService.isCountingDown()) {
                    countdownService.stopCountdown()
                    btnStart.text = "開始"
                } else {
                    countdownService.startCountdown(10)
                    btnStart.text = "停止"
                }
            }
        }
    }
}