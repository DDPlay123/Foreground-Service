package mai.project.foreground_service

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import mai.project.foreground_service.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
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

            if (countdownService.isCountingDown()) {
                binding.btnStart.text = "停止"
                binding.tvState.text = "倒數中: ${countdownService.timeRemaining} 秒"
            }
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions()
        }
        setListener()
    }

    override fun onStart() {
        super.onStart()
        Intent(this@MainActivity, CountdownService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissions() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val permissionGranted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            requestPermissions(arrayOf(permission), 0)
        }
    }

    private fun setListener() = with(binding) {
        btnStart.setOnClickListener {
            if (isBound) {
                if (countdownService.isCountingDown()) {
                    countdownService.stopCountdown()
                    btnStart.text = "開始"
                } else {
                    countdownService.startCountdown(100)
                    btnStart.text = "停止"
                }
            }
        }
    }
}