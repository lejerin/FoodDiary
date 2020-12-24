package lej.happy.fooddiary.ui.review

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import lej.happy.fooddiary.ui.custom.LoadingDialog
import lej.happy.fooddiary.data.Model.ReviewRank
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.util.Coroutines
import java.util.*
import kotlin.Comparator

class ReviewViewModel(
    private val repository: Repository
) : BaseViewModel() {

    lateinit var context: Context
    //중복없는 날짜만

    lateinit var reviewAdapter : ReviewAdapter

    val rankList = MutableLiveData<MutableList<ReviewRank>>()
    init {
        rankList.value = ArrayList()
        reviewAdapter =
            ReviewAdapter(rankList.value!!)

    }

    private var isDESC = true

    val reviewDetailFragment = ReviewDetailFragment()

    fun setOrder(order: Boolean){
        isDESC = order
        getReviewData()
    }

    lateinit var loadingDialog : LoadingDialog
    var isLoading = false

    fun getReviewData(){

        loadingDialog = LoadingDialog(context)
        loadingDialog.show()
        isLoading = true

        rankList.value?.clear()

        //해당 년도와 월에 대해서만 date, count 순서대로 가져옴
        newJob(
            Coroutines.ioThenMain(
                { filterPost(getQuery())},
                {
                    isLoading = false
                    loadingDialog.dismiss()
                    rankList.value = rankList.value
                }
            ))
    }

    private fun getQuery(): List<Post> = repository.getLocationDESC()


    private fun filterPost(data: List<Post>) {

        if (data.isNotEmpty()) {
            if (data != null) {

                var beforeName = ""
                var beforeIndex = 0
                //중복 처리
                for (i in data.indices) {

                    if(!beforeName.equals(data[i].location)){
                        beforeName = data[i].location
                        rankList.value?.add(ReviewRank(data[i],1,0,0,0))
                        beforeIndex = rankList.value!!.size - 1
                        when(data[i].taste){
                            1-> rankList.value!![beforeIndex].best += 1
                            2-> rankList.value!![beforeIndex].good += 1
                            3-> rankList.value!![beforeIndex].bad += 1
                        }
                    }else{
                        rankList.value!![beforeIndex].num += 1
                        when(data[i].taste){
                            1-> rankList.value!![beforeIndex].best += 1
                            2-> rankList.value!![beforeIndex].good += 1
                            3-> rankList.value!![beforeIndex].bad += 1
                        }
                    }

                }
                //정렬하기
                Collections.sort(rankList.value, Comparator<ReviewRank> { o1, o2 -> o1.post.date!!.compareTo(o2.post.date) })
                if(isDESC){
                    rankList.value!!.reverse();
                }

            }

        }
    }

    private val _isDetailClick = MutableLiveData<ReviewDetailFragment>()
    val isDetailClick : LiveData<ReviewDetailFragment>
        get() = _isDetailClick

    val clicksListener = object : ReviewAdapter.OnItemClickListener {
        override fun onClick(position: Int) {
            val bundle = Bundle()
            bundle.putString("address", rankList.value!![position].post.address)
            bundle.putString("name", rankList.value!![position].post.location)
            reviewDetailFragment.arguments = bundle

            _isDetailClick.value = reviewDetailFragment

        }
    }.also {
        reviewAdapter.setReviewItemClickListener(it)
    }


}