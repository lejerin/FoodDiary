package lej.happy.fooddiary.Fragment

import android.R.attr.bitmap
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import lej.happy.fooddiary.Activity.AddPostActivity
import lej.happy.fooddiary.Adapter.HomePhotoAdapter
import lej.happy.fooddiary.Adapter.PhotoGridAdapter
import lej.happy.fooddiary.DB.AppDatabase
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.Helper.ImageUtil
import lej.happy.fooddiary.Model.HomeData
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
    private val REQUEST_CODE_ADD_POST = 11



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rv_home.layoutManager =  LinearLayoutManager(context)
        rv_home.setHasFixedSize(true)
        rv_home.setItemViewCacheSize(20);


        //이번달 첫째날, 마지막날
        val nowYM = SimpleDateFormat("yyyy-M", Locale.KOREA).format(Date())
        stringToDate(nowYM)

        getHomeData()

    }


    fun addNewPost(){

        val addPostIntent = Intent(context, AddPostActivity::class.java)
        activity?.startActivityForResult(addPostIntent, REQUEST_CODE_ADD_POST)

    }

    fun setOrder(order: Boolean){
        isDESC = order
        getHomeData()
    }

    fun setHomeDate(isOk: Boolean, year: String, mon : String){
        isAll = isOk
        homePhotoAdapter.isAll = isAll
        stringToDate("$year-$mon")
        getHomeData()
    }



    fun getHomeData(){

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

        val data = getQuery()
        if(data.isNotEmpty()){

            var beforeDate = ""
            var beforeMonth = ""
            for(i in 0..data.size-1) {
                val output: String = SimpleDateFormat("yyyy-M-d", Locale.KOREA).format(data.get(i).date)
                val nowMonth: String = SimpleDateFormat("yyyy년 M월", Locale.KOREA).format(data.get(i).date)

                if(!output.equals(beforeDate)){
                    //중복 되지 않으면 time list에 넣기
                    val grid =  GridLayoutManager(context, 3)
                    if(beforeMonth.equals(nowMonth)){
                        timeList.add(HomeData(output, false, "",null,grid))
                    }else{
                        timeList.add(HomeData(output, true, nowMonth,null,grid))
                    }
                    beforeDate = output
                    beforeMonth = nowMonth
                }
                if(photoList.containsKey(output)){
                    //이미 존재하면
                    photoList.get(output)!!.add(data[i])
                }else{
                    photoList.put(output,mutableListOf(data[i]))
                }
            }

            for (i in 0..timeList.size-1){

                var photoAdapter = PhotoGridAdapter(photoList.get(timeList[i].date)!!)
                timeList[i].adapters = photoAdapter
            }

            System.out.println("계산 끝")

            return true
        }
        return false
    }


    private fun resetAdapter(isOk : Boolean){

        System.out.println("적용")

        if(::homePhotoAdapter.isInitialized){
            homePhotoAdapter.notifyDataSetChanged()
        }else{
            homePhotoAdapter = HomePhotoAdapter(timeList,isAll)
            rv_home.adapter = homePhotoAdapter
        }

        if(isOk)
            no_data_in_recyclerview.visibility = View.GONE
        else
            no_data_in_recyclerview.visibility = View.VISIBLE
    }

    fun getQuery() : List<Post>{
        val postDb = AppDatabase.getInstance(context!!)

        if(isAll){
            if(isDESC) return postDb?.postDao()?.selectByPageDesc()
            else return postDb?.postDao()?.selectByPageAsc()

        }else{
            if(isDESC) return postDb?.postDao()?.selectByDateDESC(startDate, endDate)
            else return postDb?.postDao()?.selectByDateASC(startDate, endDate)
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == 44 && resultCode == AppCompatActivity.RESULT_OK){
            Handler().postDelayed(
                {
                    System.out.println("초기화")
                    timeList.clear()
                    photoList.clear()
                    //이전에 저장했던 데이터대로 수정필요
                    getHomeData()
                },
                1000 // value in milliseconds
            )
        }

        //수정되었을 때
        if(requestCode == 77 && resultCode == AppCompatActivity.RESULT_OK){

            System.out.println("초기화")
            timeList.clear()
            photoList.clear()
            //이전에 저장했던 데이터대로 수정필요
            getHomeData()


        }

        if(requestCode == REQUEST_CODE_ADD_POST && resultCode == AppCompatActivity.RESULT_OK){
            System.out.println("초기화")
            timeList.clear()
            photoList.clear()
            //이전에 저장했던 데이터대로 수정필요
            getHomeData()
        }
    }


}