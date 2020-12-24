package lej.happy.fooddiary.ui.taste

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import lej.happy.fooddiary.data.Repository

@Suppress("UNCHECKED_CAST")
class TasteViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TasteViewModel(repository) as T
    }

}