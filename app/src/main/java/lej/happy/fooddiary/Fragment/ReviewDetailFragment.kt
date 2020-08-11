package com.example.fooddiary.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_review.*
import kotlinx.android.synthetic.main.fragment_review_detail.*
import kotlinx.android.synthetic.main.fragment_review_detail.no_data_in_recyclerview
import kotlinx.coroutines.*
import lej.happy.fooddiary.Activity.MainActivity
import lej.happy.fooddiary.Adapter.ReviewPhotoAdapter
import lej.happy.fooddiary.DB.AppDatabase
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.MyApplication
import lej.happy.fooddiary.R
import java.text.SimpleDateFormat
import java.util.*

class ReviewDetailFragment : Fragment(){

    //중복없는 날짜만
    val timeList = mutableListOf<String>()
    //고유번호, 대표사진, count 순서대로
    val photoList : HashMap<String,MutableList<Post>> = hashMapOf()
    lateinit var address : String
    private var isDESC = true



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_review_detail, container, false)

        val a = activity as MainActivity
        a.setActionBarTitle(arguments!!.getString("name").toString())
        address = arguments!!.getString("address").toString()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        var musicAdapter = ReviewPhotoAdapter(timeList,photoList)

        rv_review_detail.adapter = musicAdapter
        rv_review_detail.layoutManager = LinearLayoutManager(context)

        setAdBottomMargin()
        getReviewDetailData()

    }

    fun setAdBottomMargin(){

        val lp =  rv_review_detail.layoutParams as ConstraintLayout.LayoutParams
        lp.bottomMargin = MyApplication.prefs.getInt("adview", 200)
        rv_review_detail.layoutParams = lp

    }

    fun setOrder(order: Boolean){
        isDESC = order
        getReviewDetailData()
    }

    fun getReviewDetailData(){

        timeList.clear()
        photoList.clear()

        //해당 년도와 월에 대해서만 date, count 순서대로 가져옴
        CoroutineScope(Job() + Dispatchers.Main).launch(Dispatchers.Default) {
            val result = async {
                getDataInDb() // some background work
            }.await()
            withContext(Dispatchers.Main) {
                // some UI thread work for when the background work is done
                Handler().postDelayed(
                    {
                        resetAdapter(result)
                    },
                    300 // value in milliseconds
                )
            }
        }

    }

    private fun getDataInDb() : Boolean{
        val postDb = AppDatabase.getInstance(context!!)
        var data : List<Post>
        if(isDESC) {
            data =  postDb?.postDao()?.selectByAddressDesc(address)
        }else{
            data =  postDb?.postDao()?.selectByAddressAsc(address)
        }

        if(data.size > 0){


            var beforeDate = ""
            for(i in 0..data.size-1) {
                var output: String = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(data.get(i).date)

                if(!output.equals(beforeDate)){
                    //중복 되지 않으면 time list에 넣기
                    beforeDate = output
                    timeList.add(output)
                }
                if(photoList.containsKey(output)){
                    //이미 존재하면
                    photoList.get(output)!!.add(data.get(i))
                }else{
                    photoList.put(output, mutableListOf(data.get(i)))
                }
            }
            return true
        }
        return false
    }

    private fun resetAdapter(isOk : Boolean){

        if(isOk){

            no_data_in_recyclerview.visibility = View.GONE
            rv_review_detail.adapter?.notifyDataSetChanged()

        }else{
            rv_review_detail.adapter?.notifyDataSetChanged()
            no_data_in_recyclerview.visibility = View.VISIBLE
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //수정되었을 때
        if(requestCode == 77 && resultCode == AppCompatActivity.RESULT_OK){


            getReviewDetailData()


        }

    }


}