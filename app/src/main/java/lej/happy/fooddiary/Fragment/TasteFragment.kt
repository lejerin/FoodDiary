package lej.happy.fooddiary.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddiary.fragment.ReviewDetailFragment
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_review.*
import kotlinx.android.synthetic.main.fragment_review.review_no_data_in_recyclerview
import kotlinx.android.synthetic.main.fragment_taste.*
import kotlinx.android.synthetic.main.fragment_taste.bad_emotion
import kotlinx.android.synthetic.main.fragment_taste.bad_emotion_text
import kotlinx.android.synthetic.main.fragment_taste.best_emotion
import kotlinx.android.synthetic.main.fragment_taste.best_emotion_text
import kotlinx.android.synthetic.main.fragment_taste.good_emotion
import kotlinx.android.synthetic.main.fragment_taste.good_emotion_text
import kotlinx.coroutines.*
import lej.happy.fooddiary.Adapter.HomePhotoAdapter
import lej.happy.fooddiary.Adapter.PhotoGridAdapter
import lej.happy.fooddiary.Adapter.ReviewLocationAdapter
import lej.happy.fooddiary.DB.AppDatabase
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.Helper.LoadingDialog
import lej.happy.fooddiary.Model.HomeData
import lej.happy.fooddiary.Model.ReviewRank
import lej.happy.fooddiary.MyApplication
import lej.happy.fooddiary.R
import java.text.SimpleDateFormat
import java.util.*


class TasteFragment : Fragment() {

    //중복없는 날짜만
    var defaultList = mutableListOf<Post>()


    var isLoading = false
    private var isDESC = true

    lateinit var loadingDialog : LoadingDialog
    lateinit var tasteAdapter : PhotoGridAdapter

    var nowTaste = 1
    //선택한 맛
    private var selectEmotionLayout : ConstraintLayout? = null
    private var selectEmotionImg: ImageView? = null
    private var selectEmotionText: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_taste, container, false)

        getReviewData()



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tasteAdapter = PhotoGridAdapter(defaultList)
        ts_review.adapter = tasteAdapter
        ts_review.layoutManager = GridLayoutManager(context, 3)

        best_taste_btn.setOnClickListener {
            chageEmotionImg(best_taste_btn)
            getReviewData()
        }
        good_taste_btn.setOnClickListener {
            chageEmotionImg(good_taste_btn)
            getReviewData()
        }
        bad_taste_btn.setOnClickListener {
            chageEmotionImg(bad_taste_btn)
            getReviewData()
        }


        chageEmotionImg(best_taste_btn)

        setAdBottomMargin()
    }

    fun setOrder(order: Boolean){
        isDESC = order
        getReviewData()
    }

    fun getReviewData(){

        loadingDialog = LoadingDialog(context!!)
        loadingDialog.show()


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

    fun getQuery() : List<Post>{
        val postDb = AppDatabase.getInstance(context!!)

        if(isDESC) return postDb?.postDao()?.selectByTasteDesc(nowTaste)
        else return postDb?.postDao()?.selectByTasteAsc(nowTaste)

    }

    private fun getDataInDb() : Boolean {
        defaultList.clear()

        val data = getQuery()
        if (data.isNotEmpty()) {


            defaultList.addAll(data)

                return true
            }

            return false

    }
    fun setAdBottomMargin(){

        val lp =  ts_review.layoutParams as ConstraintLayout.LayoutParams
        lp.bottomMargin = MyApplication.prefs.getInt("adview", 200)
        ts_review.layoutParams = lp

    }

    private fun resetAdapter(isOk : Boolean){


        tasteAdapter.notifyDataSetChanged()

        isLoading = false
        loadingDialog.dismiss()

        if(defaultList.size > 0){
            taste_no_data_in_recyclerview.visibility = View.GONE
        }else{
            taste_no_data_in_recyclerview.visibility = View.VISIBLE
        }
    }

    private fun chageEmotionImg(a: ConstraintLayout){
        if(selectEmotionLayout != null){
            selectEmotionLayout!!.setBackgroundResource(R.drawable.emotion_background)
            selectEmotionText!!.setTextColor(
                ContextCompat.getColor(context!!,
                R.color.brightGray))
            selectEmotionImg!!.setColorFilter(
                ContextCompat.getColor(context!!,
                R.color.brightGray));
        }
        selectEmotionLayout = a
        if(a == best_taste_btn){
            nowTaste = 1
            selectEmotionImg = best_emotion
            selectEmotionText = best_emotion_text
        }
        else if(a == good_taste_btn) {
            nowTaste = 2
            selectEmotionImg = good_emotion
            selectEmotionText = good_emotion_text
        }
        else{
            nowTaste = 3
            selectEmotionImg = bad_emotion
            selectEmotionText = bad_emotion_text
        }


        selectEmotionLayout!!.setBackgroundResource(R.drawable.emotion_background_selected)
        selectEmotionImg!!.setColorFilter(
            ContextCompat.getColor(context!!,
            R.color.colorPrimary));
        selectEmotionText!!.setTextColor(
            ContextCompat.getColor(context!!,
            R.color.colorPrimary))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //수정되었을 때
        if(requestCode == 77 && resultCode == AppCompatActivity.RESULT_OK){


            getReviewData()


        }

    }

}