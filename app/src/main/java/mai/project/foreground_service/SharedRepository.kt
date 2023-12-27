package mai.project.foreground_service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object SharedRepository {

    private val _countdownTime = MutableLiveData<Map<Int, Long>>()
    val countdownTime: LiveData<Map<Int, Long>> = _countdownTime

    fun setCountdownTime(index: Int, time: Long) {
        _countdownTime.value = _countdownTime.value?.toMutableMap()?.apply {
            put(index, time)
        } ?: mapOf(index to time)
    }
}