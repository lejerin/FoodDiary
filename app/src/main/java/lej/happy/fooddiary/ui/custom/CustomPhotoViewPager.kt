package lej.happy.fooddiary.ui.custom

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.custom_photo_viewpager.view.*
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.ui.adapter.ViewPagerAdapter
import lej.happy.fooddiary.utils.UiUtils

open class CustomPhotoViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null, defStyleAttr: Int=0)
    : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.custom_photo_viewpager, this)
    }

    var photoList = mutableListOf<String>()
    private lateinit var photoViewPagerAdapter: ViewPagerAdapter
    var selectedNum = 0

    private var clickListener: PhotoButtonListener? = null

    //인터페이스 설정
    interface PhotoButtonListener {
        fun isAdd(isTrue: Boolean)
    }

    //호출할 리스너 초기화
    fun setButtonListener(customDialogListener: PhotoButtonListener?) {
        this.clickListener = customDialogListener
    }

    fun init(id: Long, isShowButton: Boolean){
        photoViewPagerAdapter = ViewPagerAdapter(photoList, id)
        pager.adapter = photoViewPagerAdapter
        pager.addOnPageChangeListener(viewPagerListener)

        add_photo_btn.setOnClickListener {
            clickListener?.isAdd(true)
        }
        add_photo_more_btn.setOnClickListener {
            clickListener?.isAdd(true)
        }
        remove_photo_btn.setOnClickListener {
            clickListener?.isAdd(false)
        }
        if (isShowButton) {
            changeButton()
        }
    }

    fun clearData(){
        photoList.clear()
    }

    fun setPhoto(post: Post, isShowButton: Boolean){
        photoViewPagerAdapter.setId(post.id!!)

        photoList.add(post.photo1)
        if(post.photo2 != null)  photoList.add(post.photo2!!)
        if(post.photo3 != null)  photoList.add(post.photo3!!)
        if(post.photo4 != null)  photoList.add(post.photo4!!)
        photoViewPagerAdapter.notifyDataSetChanged()

        initIndicator(0)
        if(isShowButton) changeButton()
        selectedNum = 0
    }


    private val viewPagerListener = object : ViewPager.OnPageChangeListener{
        override fun onPageScrollStateChanged(state: Int) {

        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageSelected(position: Int) {
            selectedNum = position
            circleAnimIndicator.selectDot(selectedNum)
        }
    }

    fun add(uri: Uri){
        photoList.add(uri.toString())
        photoViewPagerAdapter.notifyDataSetChanged()

        selectedNum = photoList.size-1
        pager.setCurrentItem(selectedNum, true)
        initIndicator(selectedNum)
        changeButton()
    }

    fun remove(){
        if (photoList.isEmpty()) {
            UiUtils.showCenterToast(context, "삭제할 사진이 없습니다.")
        } else {
            photoList.removeAt(selectedNum)
            photoViewPagerAdapter.notifyDataSetChanged()
            initIndicator(0)
            changeButton()
            selectedNum = 0
        }
    }

    private fun changeButton(){
        add_photo_btn.visibility = if (photoList.size <= 0) View.VISIBLE else View.INVISIBLE
        add_photo_more_btn.visibility = if (photoList.size < 4) View.VISIBLE else View.INVISIBLE
        remove_photo_btn.visibility = if (photoList.size > 0) View.VISIBLE else View.INVISIBLE
    }

    private fun initIndicator(num: Int){
        circleAnimIndicator.removeDotPanel()
        //원사이의 간격
        circleAnimIndicator.setItemMargin(15)
        //애니메이션 속도
        circleAnimIndicator.setAnimDuration(300)
        //indecator 생성
        circleAnimIndicator.createDotPanel(photoList.size, R.drawable.viewpage_indicator_off , R.drawable.viewpager_indicator_on);
        circleAnimIndicator.selectDot(num)

    }

    fun getSize(): Int {
        return if(!photoList.isNullOrEmpty()) photoList.size
        else 0
    }

}