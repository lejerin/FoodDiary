package lej.happy.fooddiary.ui.time

import android.content.Context
import android.os.Handler
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
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

    val homePhotoAdapter: HomePhotoAdapter
    val timeList = MutableLiveData<MutableList<HomeData>>()
    init {
        timeList.value = ArrayList()
        homePhotoAdapter = HomePhotoAdapter(timeList.value!!, true)
    }

    //고유번호, 대표사진, count 순서대로
    val photoList : HashMap<String,MutableList<Post>> = hashMapOf()
    lateinit var photoLiveData : LiveData<HashMap<String, MutableList<Post>>>


    lateinit var startDate : Date
    lateinit var endDate : Date
    private var isDESC = true
    private var isAll = true


    var page = 1
    var isLoading = false
    val limit = 20
    var isEnd = false
    fun initPage(){
        page = 1
        isEnd = false
        timeList.value?.clear()
        photoList.clear()
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

    fun scrollData(){

    }

    fun getTimeData(context: Context){

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
            println("dddddddddddddd" + data.size)

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