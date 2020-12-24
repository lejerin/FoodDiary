package lej.happy.fooddiary.ui.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import lej.happy.fooddiary.data.Repository

@Suppress("UNCHECKED_CAST")
class TimeViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TimeViewModel(repository) as T
    }

}