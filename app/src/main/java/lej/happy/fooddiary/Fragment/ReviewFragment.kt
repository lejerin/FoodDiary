package lej.happy.fooddiary.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddiary.fragment.ReviewDetailFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_review.*
import kotlinx.coroutines.*
import lej.happy.fooddiary.Adapter.ReviewLocationAdapter
import lej.happy.fooddiary.DB.AppDatabase
import lej.happy.fooddiary.Helper.LoadingDialog
import lej.happy.fooddiary.Model.ReviewRank
import lej.happy.fooddiary.MyApplication
import lej.happy.fooddiary.R
import java.util.*


class ReviewFragment : Fragment() {

    //중복없는 날짜만
    val rankList = mutableListOf<ReviewRank>()
    // val duplePostList : HashMap<String, Post> = hashMapOf()

    private var isDESC = true

    lateinit var loadingDialog : LoadingDialog


    val reviewDetailFragment = ReviewDetailFragment()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_review, container, false)



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {



        var reviewAdapter = ReviewLocationAdapter(rankList)
        reviewAdapter.setReviewItemClickListener(object : ReviewLocationAdapter.OnItemClickListener{
            override fun onClick(position: Int) {

                val bundle = Bundle()
                bundle.putString("address", rankList.get(position).post.address)
                bundle.putString("name", rankList.get(position).post.location)
                reviewDetailFragment.arguments = bundle

                val transaction = getFragmentManager()!!.beginTransaction()
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left,R.anim.exit_to_right)
                transaction.add(R.id.frame_layout, reviewDetailFragment)
                transaction.addToBackStack("review_detail")
                transaction.commit();

            }
        })
        rv_review.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        rv_review.adapter = reviewAdapter
        rv_review.layoutManager = LinearLayoutManager(context)

        setAdBottomMargin()

        getReviewData()
    }

    fun setAdBottomMargin(){

        val lp =  rv_review.layoutParams as ConstraintLayout.LayoutParams
        lp.bottomMargin = MyApplication.prefs.getInt("adview", 200)
        rv_review.layoutParams = lp

    }

    fun setOrder(order: Boolean){
        isDESC = order
        getReviewData()
    }

    fun getReviewData(){

        loadingDialog = LoadingDialog(context!!)
        loadingDialog.show()

        rankList.clear()

        //해당 년도와 월에 대해서만 date, count 순서대로 가져옴
        CoroutineScope(Job() + Dispatchers.Main).launch(Dispatchers.Default) {
            async {
                getDataInDb() // some background work
            }.await()
            withContext(Dispatchers.Main) {
                // some UI thread work for when the background work is done
                Handler().postDelayed(
                    {
                        resetAdapter()
                    },
                    300 // value in milliseconds
                )
            }
        }

    }

    private fun getDataInDb(){
        val postDb = AppDatabase.getInstance(context!!)
        val data =  postDb?.postDao()?.selectByLocationDesc()

        if (data != null) {

            var beforeName = ""
            var beforeIndex = 0
            //중복 처리
            for (i in 0..data.size - 1) {

                if(!beforeName.equals(data[i].location)){
                    beforeName = data[i].location
                    rankList.add(ReviewRank(data[i],1,0,0,0))
                    beforeIndex = rankList.size -1
                    when(data[i].taste){
                        1-> rankList[beforeIndex].best += 1
                        2-> rankList[beforeIndex].good += 1
                        3-> rankList[beforeIndex].bad += 1
                    }
                }else{
                    rankList[beforeIndex].num += 1
                    when(data[i].taste){
                        1-> rankList[beforeIndex].best += 1
                        2-> rankList[beforeIndex].good += 1
                        3-> rankList[beforeIndex].bad += 1
                    }
                }

            }
            //정렬하기
            Collections.sort(rankList, Comparator<ReviewRank> { o1, o2 -> o1.post.date!!.compareTo(o2.post.date) })
            if(isDESC){
                Collections.reverse(rankList);
            }

        }

    }

    private fun resetAdapter(){

        if(rankList.size > 0){

            review_no_data_in_recyclerview.visibility = View.INVISIBLE
            rv_review.adapter?.notifyDataSetChanged()

        }else{
            rv_review.adapter?.notifyDataSetChanged()
            review_no_data_in_recyclerview.visibility = View.VISIBLE
        }

        loadingDialog.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //수정되었을 때
        if(requestCode == 77 && resultCode == AppCompatActivity.RESULT_OK){


            getReviewData()
            reviewDetailFragment.onActivityResult(requestCode, resultCode, data)


        }

    }

}