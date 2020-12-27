package lej.happy.fooddiary.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import lej.happy.fooddiary.data.Repository

@Suppress("UNCHECKED_CAST")
class ReviewDetailViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReviewDetailViewModel(repository) as T
    }

}