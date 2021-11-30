package lej.happy.fooddiary.ui.location

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.SingleLiveEvent
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.data.local.repository.DateRepos
import lej.happy.fooddiary.data.local.repository.LocationRepos
import lej.happy.fooddiary.data.model.HomeData
import lej.happy.fooddiary.data.model.ReviewRank
import lej.happy.fooddiary.ui.adapter.PhotoGridAdapter
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.utils.Coroutines
import org.koin.java.KoinJavaComponent.inject
import java.text.SimpleDateFormat
import java.util.*


class LocationViewModel : BaseViewModel() {
    private val TAG = LocationViewModel::class.java.simpleName

    /** Repos */
    private val mLocationRepos by inject(LocationRepos::class.java)

    var isDESC = true

    var page = 0
    var isEnd = false
    var isLoading = false

    var newDataList: SingleLiveEvent<Pair<BaseValue.DATA_TYPE, List<ReviewRank>?>> = SingleLiveEvent()

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
        Log.i(TAG, "getQuery : isDESC $isDESC\npage $page")
        return if (isDESC) {
            mLocationRepos.getLocationDESC(page)
        } else {
            mLocationRepos.getLocationASC(page)
        }
    }

    private fun filterPost(data: List<Post>): MutableList<ReviewRank> {
        Log.i(TAG, "filterPost : data $data")
        val list = mutableListOf<ReviewRank>()
        if(data.isNotEmpty()){
            var beforeAddress = ""
            for (i in data.indices) {
                data[i].address?.let {
                    if (it != beforeAddress || list.isEmpty()) {
                        beforeAddress = it
                        list.add(ReviewRank(data[i], 1, 0, 0, 0))
                    } else {
                        list.last().num += 1
                    }
                    when (data[i].taste) {
                        1-> list.last().best += 1
                        2-> list.last().good += 1
                        3-> list.last().bad += 1
                    }
                }
            }
            if(data.size < BaseValue.limit){
                isEnd = true
            }
        } else {
            isEnd = true
        }
        return list
    }
}