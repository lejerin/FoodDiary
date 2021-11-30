package lej.happy.fooddiary.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

open class BaseViewModel : ViewModel() {

    private lateinit var job: Job


    fun setJob(newJob: Job) {
        job = newJob
    }

    override fun onCleared() {
        super.onCleared()
        if(::job.isInitialized) job.cancel()
    }
}