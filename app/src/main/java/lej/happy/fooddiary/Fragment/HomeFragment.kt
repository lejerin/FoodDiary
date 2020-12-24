package lej.happy.fooddiary.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import lej.happy.fooddiary.Activity.AddPostActivity
import lej.happy.fooddiary.Adapter.HomePhotoAdapter
import lej.happy.fooddiary.Adapter.PhotoGridAdapter
import lej.happy.fooddiary.DB.AppDatabase
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.Helper.LoadingDialog
import lej.happy.fooddiary.Model.HomeData
import lej.happy.fooddiary.MyApplication
import lej.happy.fooddiary.R
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {

    //중복없는 날짜만
    val timeList = mutableListOf<HomeData>()
    //고유번호, 대표사진, count 순서대로
    val photoList : HashMap<String,MutableList<Post>> = hashMapOf()

    lateinit var startDate : Date
    lateinit var endDate : Date
    private var isDESC = true
    private var isAll = true

    private lateinit var homePhotoAdapter: HomePhotoAdapter
    private lateinit var homeLayoutManager: LinearLayoutManager
    private val REQUEST_CODE_ADD_POST = 11

    var page = 1
    var isLoading = false
    val limit = 20
    var isEnd = false

    lateinit var loadingDialog : LoadingDialog

    lateinit var rv_home: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rv_home = view.findViewById(R.id.rv_home)

        homeLayoutManager = LinearLayoutManager(context)
        rv_home.layoutManager =  homeLayoutManager
        rv_home.setHasFixedSize(true)
        rv_home.setItemViewCacheSize(20);

        //이번달 첫째날, 마지막날
        val nowYM = SimpleDateFormat("yyyy-M", Locale.KOREA).format(Date())
        stringToDate(nowYM)






        getHomeData()

    }


    fun setOrder(order: Boolean){
        isDESC = order
        initPage()
        getHomeData()
    }

    fun setHomeDate(isOk: Boolean, year: String, mon : String){
        isAll = isOk
        if(isOk){
            initPage()
        }
        homePhotoAdapter.isAll = isAll
        stringToDate("$year-$mon")
        getHomeData()
    }




}