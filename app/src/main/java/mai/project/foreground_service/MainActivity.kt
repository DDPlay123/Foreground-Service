package mai.project.foreground_service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import mai.project.foreground_service.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions()
        }
        setListener()
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
            val serviceIntent = Intent(this@MainActivity, CountdownService::class.java).apply {
                action = CountdownService.ACTION_START
                putExtra(CountdownService.EXTRA_COUNTDOWN_TIME, 100L)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }
}