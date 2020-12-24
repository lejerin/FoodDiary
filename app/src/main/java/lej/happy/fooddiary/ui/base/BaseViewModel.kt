package lej.happy.fooddiary.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

open class BaseViewModel : ViewModel(){

    private lateinit var job: Job

    fun newJob(new_job: Job) {
        job = new_job
    }

    override fun onCleared() {
        super.onCleared()
        if(::job.isInitialized) job.cancel()
    }
}