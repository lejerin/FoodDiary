package lej.happy.fooddiary.ui.time

import android.content.Context
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import lej.happy.fooddiary.Adapter.HomePhotoAdapter
import lej.happy.fooddiary.Adapter.PhotoGridAdapter
import lej.happy.fooddiary.DB.AppDatabase
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.Helper.LoadingDialog
import lej.happy.fooddiary.Model.HomeData
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.ui.MainViewModel
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.util.Coroutines
import java.text.SimpleDateFormat
import java.util.*

class TimeViewModel(
    private val repository: Repository
) : BaseViewModel() {


    lateinit var homeLayoutManager: LinearLayoutManager
    val homePhotoAdapter: HomePhotoAdapter
    val timeList = MutableLiveData<MutableList<HomeData>>()
    init {
        timeList.value = ArrayList()
        homePhotoAdapter = HomePhotoAdapter(timeList.value!!, true)
    }

    //고유번호, 대표사진, count 순서대로
    val photoList : HashMap<String,MutableList<Post>> = hashMapOf()

    lateinit var startDate : Date
    lateinit var endDate : Date
    private var isDESC = true
    private var isAll = true


    var page = 1
    var isLoading = false
    val limit = 20
    var isEnd = false

    lateinit var context: Context

    fun initPage(){
        page = 1
        isEnd = false
        timeList.value?.clear()
        photoList.clear()
    }

    fun setOrder(order: Boolean){
        isDESC = order
        initPage()
        getTimeData()
    }

    fun setHomeDate(isMonth: Boolean, year: String, mon : String){
        isAll = !isMonth
        if(isMonth){
            println("init home")
            initPage()
        }
        homePhotoAdapter.isAll = isAll
        stringToDate("$year-$mon")
        getTimeData()
    }

    private fun getQuery(): List<Post> {

        return if(isAll) {
            if(isDESC) repository.getDataAllDESC(page, limit)
            else repository.getDataAllASC(page, limit)
        }else {
            if(isDESC) repository.getDataMonthDESC(startDate, endDate)
            else repository.getDataMonthASC(startDate, endDate)
        }
    }

    lateinit var loadingDialog : LoadingDialog


    fun getTimeData(){

        loadingDialog = LoadingDialog(context)
        loadingDialog.show()


        if(isAll){
            isLoading = true

        }else{
            timeList.value?.clear()
            photoList.clear()
        }

        //해당 년도와 월에 대해서만 date, count 순서대로 가져옴
        newJob(Coroutines.ioThenMain(
            { filterPost(getQuery(), context) },
            {
                isLoading = false
                loadingDialog.dismiss()
                timeList.value = timeList.value
            }
        ))
    }

    private fun filterPost(data: List<Post>, context: Context){

        if(data.isNotEmpty()){

            var beforeDate = ""
            var beforeMonth = ""
            for(i in data.indices) {
                val output: String = SimpleDateFormat("yyyy-M-d", Locale.KOREA).format(data[i].date)
                val nowMonth: String = SimpleDateFormat("yyyy년 M월", Locale.KOREA).format(data[i].date)

                if(output != beforeDate){
                    //중복 되지 않으면 time list에 넣기
                    val grid =  GridLayoutManager(context, 3)
                    if(beforeMonth == nowMonth){
                        timeList.value?.add(HomeData(output, false, "",null, grid))
                    }else{
                        timeList.value?.add(HomeData(output, true, nowMonth,null, grid))
                    }
                    beforeDate = output
                    beforeMonth = nowMonth
                }
                if(photoList.containsKey(output)){
                    //이미 존재하면
                    photoList[output]!!.add(data[i])
                }else{
                    photoList[output] = mutableListOf(data[i])
                }
            }

            for(i in 0 until timeList.value?.size!!){
                PhotoGridAdapter(photoList[timeList.value!![i].date]!!).also {
                    timeList.value!![i].adapters = it
                }
            }

            if(data.size < limit){
                isEnd = true
            }
        }else{
            isEnd = true
        }

    }

    val clicksListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            if(dy > 0 && !isEnd){
                val visibleItemCount = homeLayoutManager.childCount
                val pastVisibleItem = homeLayoutManager.findFirstCompletelyVisibleItemPosition()
                val total = homePhotoAdapter.itemCount

                if(!isLoading){
                    if((visibleItemCount + pastVisibleItem) >= total){
                        page++
                        getTimeData()
                    }
                }
            }

            super.onScrolled(recyclerView, dx, dy)
        }

    }



    fun stringToDate(str: String) {
        val dateFormat: String = "yyyy-MM-dd"
        val timeZone: TimeZone = TimeZone.getDefault()
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone

        startDate = parser.parse(str+"-1")

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        calendar.add(Calendar.MONTH, 1)
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar.add(Calendar.DATE, -1)

        val lastDayOfMonth = calendar.time

        endDate = lastDayOfMonth
    }


}