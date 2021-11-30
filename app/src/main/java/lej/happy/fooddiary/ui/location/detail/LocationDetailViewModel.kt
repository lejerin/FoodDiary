package lej.happy.fooddiary.ui.location.detail

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
import lej.happy.fooddiary.data.local.repository.LocationRepos
import lej.happy.fooddiary.data.model.HomeData
import lej.happy.fooddiary.ui.adapter.PhotoGridAdapter
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.utils.Coroutines
import org.koin.java.KoinJavaComponent.inject
import java.text.SimpleDateFormat
import java.util.*

class LocationDetailViewModel : BaseViewModel() {
    private val TAG = LocationDetailViewModel::class.java.simpleName

    /** Repos */
    private val mLocationRepos by inject(LocationRepos::class.java)

    lateinit var address: String

    var isDESC = true

    var page = 0
    var isEnd = false

    val newDataList: SingleLiveEvent<Pair<BaseValue.DATA_TYPE, LinkedHashMap<String, HomeData>?>> =
        SingleLiveEvent()


    val mutex = Mutex()

    fun getDateData(context: Context?, type: BaseValue.DATA_TYPE) {
        if (mutex.isLocked) {
            return
        }
        setJob(
            CoroutineScope(Dispatchers.IO).launch {
                mutex.withLock {
                    page++
                    val result = filterPost(context, getQuery())
                    CoroutineScope(Dispatchers.Main).launch {
                        newDataList.value = Pair(type, result)
                    }
                }
            }
        )
    }

    private fun getQuery(): List<Post> {
        return if (isDESC) mLocationRepos.getAddressDESC(address, page)
        else mLocationRepos.getAddressASC(address, page)
    }

    private fun filterPost(context: Context?, data: List<Post>): LinkedHashMap<String, HomeData> {
        Log.i(TAG, "filterPost : data $data")
        val hashMapList: LinkedHashMap<String, HomeData> = linkedMapOf()
        if (data.isNotEmpty()) {
            var beforeDate = ""
            var beforeMonth = ""
            for (i in data.indices) {
                data[i].date?.let {
                    val output: String = SimpleDateFormat("yyyy-M-d", Locale.KOREA).format(it)
                    val nowMonth: String = SimpleDateFormat("yyyy년 M월", Locale.KOREA).format(it)
                    if (output != beforeDate) {
                        //중복 되지 않으면 time list에 넣기
                        if (beforeMonth == nowMonth) {
                            hashMapList[output] =
                                (HomeData(output, false, "", null, null, mutableListOf(data[i])))
                        } else {
                            hashMapList[output] = (HomeData(
                                output,
                                true,
                                nowMonth,
                                null,
                                null,
                                mutableListOf(data[i])
                            ))
                        }
                        beforeDate = output
                        beforeMonth = nowMonth
                    } else {
                        hashMapList[output]?.postList?.add(data[i])
                    }
                }
            }
            if (data.size < BaseValue.limit) {
                isEnd = true
            }
        } else {
            isEnd = true
        }
        return hashMapList
    }
}