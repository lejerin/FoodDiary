package lej.happy.fooddiary.ui.post

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import kotlinx.android.synthetic.main.activity_add_post.*
import androidx.lifecycle.Observer
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.databinding.ActivityAddPostBinding
import lej.happy.fooddiary.ui.base.BaseActivity
import lej.happy.fooddiary.ui.custom.CustomPhotoViewPager
import lej.happy.fooddiary.ui.custom.LoadingDialog
import lej.happy.fooddiary.util.CameraUtil
import java.text.SimpleDateFormat
import java.util.*

class AddPostActivity : BaseActivity<ActivityAddPostBinding>() {


    override val layoutResourceId: Int
        get() = R.layout.activity_add_post

    private val viewModel: PostViewModel by viewModels()
    private var isModify = false

    lateinit var loadingDialog : LoadingDialog

    override fun initStartView() {

    }

    override fun initDataBinding() {
        viewModel.repository =  Repository(this@AddPostActivity)
        viewDataBinding.viewModel = viewModel

        //뷰페이저
        viewpager.init()
        viewpager.setButtonListener(object : CustomPhotoViewPager.PhotoButtonListener{
            override fun isAdd(isTrue: Boolean) {
                if(isTrue){
                    viewModel.addPhoto(viewpager)
                }else{
                    viewModel.removePhoto(viewpager)
                }
            }
        })
    }

    override fun initAfterBinding() {

        viewModel.message.observe(this, Observer {
            showToast(it)
        })

        viewModel.dateStr.observe(this, Observer {
            add_date_text.text = it
        })

        viewModel.isSaving.observe(this, Observer {
            //로딩화면 시작
//            if(it){
//                loadingDialog = LoadingDialog(this)
//                loadingDialog.show()
//            }else{
//                if(::loadingDialog.isInitialized){
//                    loadingDialog.dismiss()
//                }
//            }

        })

        if(intent.getSerializableExtra("post") != null){
            //수정
            isModify = true
            viewModel.post = (intent.getSerializableExtra("post") as Post).also {
                viewModel.dateStr.value = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(it.date)
            }

            //본문,사진,위치,주소,위치,시간대,평가 표시
            setModifyData()

        }else{
            //신규 추가
            //오늘 날짜로 초기값 설정
            viewModel.dateStr.value = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(Date()).also {
                viewModel.post.date = StringtoDate(it!!)
            }
        }


        save_post_btn.setOnClickListener {
            viewModel.saveData(viewpager.photoList, this)
        }

    }

    private fun setModifyData(){
        viewpager.setPhoto(viewModel.post)
        viewModel.setModifyData()
        initTaste(viewModel.post.taste)
    }


    private fun initTaste(num: Int?){

        if(num != null){
            when(num){
                1 -> {
                    viewModel.selectedBtn(best_emotion_layout)
                }
                2 -> {
                    viewModel.selectedBtn(good_emotion_layout)
                }
                3 -> {
                    viewModel.selectedBtn(bad_emotion_layout)
                }
            }
        }
    }


    val REQUEST_IMAGE_CAPTURE = 1  //카메라 사진 촬영 요청 코드 *임의로 값 입력
    val REQUEST_IMAGE_PICK = 10
    private val REQUEST_CODE_OPEN_MAP_SEARCH = 44
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //사진을 갖고왔을 때
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_PICK && data != null) {
            viewpager.add(data.data!!)
        }
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){

            val uri = CameraUtil.getInstance(this).makeBitmap()
            viewpager.add(uri)
        }

        //주소 검색을 완료했을 때
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_OPEN_MAP_SEARCH && data != null) {
            viewModel.post.address = data.getStringExtra("name")
            add_address_text.text = viewModel.post.address
            add_address_text.visibility = View.VISIBLE
            viewModel.post.location = data.getStringExtra("roadAddress")
            location_title_text.setText(viewModel.post.location)

            viewModel.post.x = data.getStringExtra("x").toDouble()
            viewModel.post.y = data.getStringExtra("y").toDouble()

        }

    }


    private fun StringtoDate(str: String, dateFormat: String = "yyyy년 M월 d일", timeZone: TimeZone = TimeZone.getDefault()): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(str)
    }
}