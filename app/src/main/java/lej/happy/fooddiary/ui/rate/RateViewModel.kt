package lej.happy.fooddiary.ui.rate

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.SingleLiveEvent
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.data.local.repository.RateRepos
import lej.happy.fooddiary.data.model.ReviewRank
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.utils.Coroutines
import org.koin.java.KoinJavaComponent.inject

class RateViewModel : BaseViewModel() {
    private val TAG = RateViewModel::class.java.simpleName

    /** Repos */
    private val mRateRepos by inject(RateRepos::class.java)

    var isDESC = true
    var taste = 1

    var page = 0
    var isEnd = false

    var newDataList: SingleLiveEvent<Pair<BaseValue.DATA_TYPE, List<Post>?>> = SingleLiveEvent()

    val mutex = Mutex()

    fun getDateData(type: BaseValue.DATA_TYPE) {
        if (mutex.isLocked) {
            return
        }
        setJob(
            CoroutineScope(Dispatchers.IO).launch {
                mutex.withLock {
                    page++
                    val result = filterPost(getQuery())
                    CoroutineScope(Dispatchers.Main).launch {
                        newDataList.value = Pair(type, result)
                    }
                }
            }
        )
    }

    private fun getQuery(): List<Post> {
        Log.i(TAG, "getQuery : taste $taste isDESC $isDESC\npage $page")
        return if (isDESC) {
            mRateRepos.getRateDESC(taste, page)
        } else {
            mRateRepos.getRateASC(taste, page)
        }
    }

    private fun filterPost(data: List<Post>): List<Post> {
        Log.i(TAG, "filterPost : data $data")
        if (data.isNotEmpty() ){
            if(data.size < BaseValue.limit){
                isEnd = true
            }
        } else {
            isEnd = true
        }
        return data
    }
}