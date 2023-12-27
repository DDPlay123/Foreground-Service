package mai.project.foreground_service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import mai.project.foreground_service.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val adapter by lazy { CountdownAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions()
        initRecyclerView()
        setObserver()
        setListener()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val permissionGranted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                requestPermissions(arrayOf(permission), 0)
            }
        }
    }

    private fun initRecyclerView() = with(binding) {
        rvTimer.adapter = adapter
        rvTimer.layoutManager = LinearLayoutManager(this@MainActivity)
    }

    private fun setObserver() = with(SharedRepository) {
        countdownTime.observe(this@MainActivity) {
            adapter.submitList(it.toList())
        }
    }

    private fun setListener() = with(binding) {
        btnStart.setOnClickListener {
            val serviceIntent = Intent(this@MainActivity, CountdownService::class.java).apply {
                action = CountdownService.ACTION_START
                putExtra(CountdownService.EXTRA_COUNTDOWN_TIME, 20L)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }
}