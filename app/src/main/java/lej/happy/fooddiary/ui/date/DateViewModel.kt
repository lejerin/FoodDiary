package lej.happy.fooddiary.ui.date

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.model.HomeData
import lej.happy.fooddiary.data.SingleLiveEvent
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.data.local.repository.DateRepos
import lej.happy.fooddiary.ui.adapter.PhotoGridAdapter
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.utils.Coroutines
import org.koin.java.KoinJavaComponent.inject
import java.text.SimpleDateFormat
import java.util.*

class DateViewModel : BaseViewModel() {
    private val TAG = DateViewModel::class.java.simpleName

    /** Repos */
    private val mDateRepos by inject(DateRepos::class.java)

    var startDate : Date? = null
    var endDate : Date? = null
    var isDESC = true

    var isAllMode = true
    var page = 0
    var isEnd = false

    val newDataList: SingleLiveEvent<Pair<BaseValue.DATA_TYPE, LinkedHashMap<String, HomeData>?>> = SingleLiveEvent()

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
        Log.i("EUNJIN", "getQuery : isAllMode $isAllMode\nisDESC $isDESC\n" +
                "startDate $startDate endDate $endDate\n" +
                "page $page" +
        "value ${(page-1) * BaseValue.limit}, ${(page) * BaseValue.limit}")
        return if (isAllMode) {
            if (isDESC) mDateRepos.getDataAllDESC(page)
            else mDateRepos.getDataAllASC(page)
        } else {
            if (isDESC) mDateRepos.getDataMonthDESC(startDate!!, endDate!!, page)
            else mDateRepos.getDataMonthASC(startDate!!, endDate!!, page)
        }
    }

    private fun filterPost(context: Context?, data: List<Post>): LinkedHashMap<String, HomeData> {
        Log.i(TAG, "filterPost : data $data")
        val hashMapList: LinkedHashMap<String, HomeData> = linkedMapOf()
        if(data.isNotEmpty()){
            var beforeDate = ""
            var beforeMonth = ""
            for(i in data.indices) {
                data[i].date?.let {
                    val output: String = SimpleDateFormat("yyyy-M-d", Locale.KOREA).format(it)
                    val nowMonth: String = SimpleDateFormat("yyyy년 M월", Locale.KOREA).format(it)
                    if (output != beforeDate) {
                        //중복 되지 않으면 time list에 넣기
                        if (beforeMonth == nowMonth) {
                            hashMapList[output] = (HomeData(output, false, "",null, null,
                                linkedSetOf<Post>().apply { add(data[i]) }
                            ))
                        } else{
                            hashMapList[output] = (HomeData(output, true, nowMonth,null, null, linkedSetOf<Post>().apply { add(data[i]) }
                            ))
                        }
                        beforeDate = output
                        beforeMonth = nowMonth
                    } else {
                        hashMapList[output]?.postList?.add(data[i])
                    }
                }
            }
            if(data.size < BaseValue.limit){
                isEnd = true
            }
        } else {
            isEnd = true
        }
        return hashMapList
    }

    fun setDate(str: String) {
        val dateFormat = "yyyy-MM-dd"
        val timeZone: TimeZone = TimeZone.getDefault()
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone

        startDate = parser.parse("$str-1")
        startDate?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.add(Calendar.MONTH, 1)
            calendar[Calendar.DAY_OF_MONTH] = 1
            calendar.add(Calendar.DATE, -1)

            endDate = calendar.time
        }
    }
}