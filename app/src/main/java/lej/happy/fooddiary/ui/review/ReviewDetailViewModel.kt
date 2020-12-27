package lej.happy.fooddiary.ui.review

import android.content.Context
import androidx.lifecycle.MutableLiveData
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.util.Coroutines
import java.text.SimpleDateFormat
import java.util.*

class ReviewDetailViewModel(
    private val repository: Repository
) : BaseViewModel() {


    //고유번호, 대표사진, count 순서대로

    lateinit var address : String
    private var isDESC = true

    lateinit var context: Context

    lateinit var musicAdapter : ReviewDetailAdapter
    lateinit var photoList : HashMap<String, MutableList<Post>>

    val timeList = MutableLiveData<MutableList<String>>()
    init {
        timeList.value = ArrayList()
        photoList  = hashMapOf()
        musicAdapter = ReviewDetailAdapter(
            timeList.value!!,
            photoList
        )
    }



    var isLoading = false

    fun getReviewDetailData(){

        if(!isLoading){


        isLoading = true

        timeList.value?.clear()
        photoList.clear()

        //해당 년도와 월에 대해서만 date, count 순서대로 가져옴
        newJob(
            Coroutines.ioThenMain(
                { filterPost(getQuery())},
                {
                    isLoading = false
                    timeList.value = timeList.value
                }
            ))
        }
    }

    private fun getQuery(): List<Post> {
        return if(isDESC) repository.getAddressDESC(address)
        else repository.getAddressASC(address)
    }


    private fun filterPost(data: List<Post>) {

        if (data.isNotEmpty()) {
                var beforeDate = ""
                for(i in data.indices) {
                    val output: String = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(data[i].date)

                    if(output != beforeDate){
                        //중복 되지 않으면 time list에 넣기
                        beforeDate = output
                        timeList.value?.add(output)
                    }
                    if(photoList.containsKey(output)){
                        //이미 존재하면
                        photoList[output]!!.add(data[i])
                    }else{
                        photoList[output] = mutableListOf(data[i])
                    }
                }

        }
    }

    fun setOrder(order: Boolean){
        isDESC = order
        getReviewDetailData()
    }
}