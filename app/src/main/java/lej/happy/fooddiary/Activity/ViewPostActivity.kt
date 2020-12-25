package lej.happy.fooddiary.Activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_detail_post.*
import kotlinx.android.synthetic.main.activity_detail_post.add_date_text
import kotlinx.android.synthetic.main.activity_detail_post.circleAnimIndicator
import kotlinx.android.synthetic.main.activity_detail_post.viewpager
import kotlinx.coroutines.*
import lej.happy.fooddiary.Adapter.ViewPagerAdapter
import lej.happy.fooddiary.data.db.AppDatabase
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.R
import lej.happy.fooddiary.ui.post.AddPostActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ViewPostActivity : AppCompatActivity(),  View.OnClickListener{

    private lateinit var thisPost: Post
    private val REQUEST_CODE_MODIFY_POST = 66

    private var isModified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_post)

        thisPost = (intent.getSerializableExtra("post") as Post)!!


        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                circleAnimIndicator.selectDot(position);
            }

        })

        initView()

        //뒤로가기
        view_detail_back_btn.setOnClickListener(this)

        //지도 상세보기
        detail_post_location_text.setOnClickListener(this)

        //수정, 삭제하기 버튼
        detail_post_more_btn.setOnClickListener(this)


    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.view_detail_back_btn -> {
                if(isModified){
                    setResult(Activity.RESULT_OK)
                 //   MainActivity2.getInstancem().refreshHome(77,RESULT_OK,null)
                }
                supportFinishAfterTransition()
            }
            R.id.detail_post_location_text -> {
                if(thisPost.address != null){
                    val mapDetailIntent = Intent(this, MapDetailActivity::class.java)
                    mapDetailIntent.putExtra("x", thisPost.x)
                    mapDetailIntent.putExtra("y", thisPost.y)
                    mapDetailIntent.putExtra("name", thisPost.location)
                    mapDetailIntent.putExtra("address", thisPost.address)
                    startActivity(mapDetailIntent)
                }

            }
            R.id.detail_post_more_btn -> {
                showPopupMenuOrder()
            }
        }
    }


    private fun showPopupMenuOrder(){
        val popup = PopupMenu(this@ViewPostActivity, detail_post_more_btn)
        popup.inflate(R.menu.post_item)
        //adding click listener
        popup.setOnMenuItemClickListener { item ->

            when (item.itemId) {
                R.id.modifyBtn -> {

                    val modifyIntent = Intent(this, AddPostActivity::class.java)
                    modifyIntent.putExtra("post", thisPost)
                    startActivityForResult(modifyIntent, REQUEST_CODE_MODIFY_POST)

                    true
                }
                R.id.deleteBtn -> {
                    showDeletePostDialog()

                    true
                }
                else -> false
            }
        }
        popup.show()
    }



    override fun onBackPressed() {
        super.onBackPressed()
        if(isModified){
            setResult(Activity.RESULT_OK)
           // MainActivity2.getInstancem().refreshHome(77,RESULT_OK,null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        println("dddd" + (resultCode == Activity.RESULT_OK))

        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_MODIFY_POST && data != null){
            isModified = true
            thisPost = (data.getSerializableExtra("modifyPost") as Post)!!
            initView()
        }

    }


    private fun initView(){
        //뷰페이저 init

        val photoList = mutableListOf<Uri>()
        photoList.add(Uri.parse(thisPost.photo1))
        if(thisPost.photo2 != null)  photoList.add(Uri.parse(thisPost.photo2))
        if(thisPost.photo3 != null)  photoList.add(Uri.parse(thisPost.photo3))
        if(thisPost.photo4 != null)  photoList.add(Uri.parse(thisPost.photo4))
        viewpager.adapter = ViewPagerAdapter(photoList as ArrayList<Uri>, thisPost.id!!)

        circleAnimIndicator.removeDotPanel()
        //원사이의 간격
        circleAnimIndicator.setItemMargin(15);
        //애니메이션 속도
        circleAnimIndicator.setAnimDuration(300);
        //indecator 생성
        circleAnimIndicator.createDotPanel(photoList.size, R.drawable.viewpage_indicator_off , R.drawable.viewpager_indicator_on);


        //상단 날짜 변경
        add_date_text.text = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(thisPost.date)
        detail_post_location_text.text = thisPost.location
        detail_post_location_text.visibility = View.VISIBLE
        if(thisPost.address != null){
            detail_post_location_text.setTextColor(
                ContextCompat.getColor(this,
                R.color.blue))
            location_tag.setTextColor(
                ContextCompat.getColor(this,
                    R.color.blue))
        }

        initTime(thisPost.time)
        initTaste(thisPost.taste)
        detail_post_text.text = thisPost.texts
    }

    private fun showDeletePostDialog(){
        val builder = AlertDialog.Builder(this@ViewPostActivity)
        builder.setMessage("정말로 삭제하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                setDeleteData()
            }
            .setNegativeButton("No") { dialog, id ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun setDeleteData(){

        CoroutineScope(Job() + Dispatchers.Main).launch(Dispatchers.Default) {
            async {
                val postDb = AppDatabase.getInstance(applicationContext)
                postDb.postDao().deleteById(thisPost.id!!)
            }.await()
            withContext(Dispatchers.Main) {
                //MainActivity2.getInstance().refreshHome(77,RESULT_OK,null)
                finish()
            }
        }

    }

    private fun initTime(num: Int?){

        if(num != null){
            when(num){
                1 -> detail_post_time_text.text = "아침"
                2 -> detail_post_time_text.text = "점심"
                3 -> detail_post_time_text.text = "저녁"
                4 -> detail_post_time_text.text = "야식"
                5 -> detail_post_time_text.text = "간식"

            }
            tag_time_text.visibility = View.VISIBLE
            detail_post_time_text.visibility = View.VISIBLE
        }


    }

    private fun initTaste(num: Int?){
        if(num != null){

        when(num){
            1 -> {
                detail_post_taste_img.setImageResource(R.drawable.laughing)
                detail_post_taste_text.text = "최고"
            }
            2 -> {
                detail_post_taste_img.setImageResource(R.drawable.happy)
                detail_post_taste_text.text = "만족"
            }
            3 -> {
                detail_post_taste_img.setImageResource(R.drawable.nervous)
                detail_post_taste_text.text = "별로"
            }
        }
        detail_post_taste_img.visibility = View.VISIBLE
        detail_post_taste_text.visibility = View.VISIBLE

        }
    }
}