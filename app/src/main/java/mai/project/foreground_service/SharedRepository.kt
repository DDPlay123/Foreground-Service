package mai.project.foreground_service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object SharedRepository {

    private val _countdownTime = MutableLiveData<Long>()
    val countdownTime: LiveData<Long> = _countdownTime

    fun setCountdownTime(time: Long) {
        _countdownTime.value = time
    }
}