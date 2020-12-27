package lej.happy.fooddiary.ui.custom

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.custom_photo_viewpager.view.*
import lej.happy.fooddiary.ui.post.ViewPagerAdapter
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.db.entity.Post

open class CustomPhotoViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null, defStyleAttr: Int=0)
    : LinearLayout(context, attrs, defStyleAttr) {

    companion object {

    }

    init {
        inflate(context, R.layout.custom_photo_viewpager, this)
    }

    var photoList = mutableListOf<Uri>()
    private val photoViewPagerAdapter =
        ViewPagerAdapter(
            photoList as ArrayList<Uri>,
            0
        )
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

    fun init(){
        pager.adapter = photoViewPagerAdapter
        pager.addOnPageChangeListener(viewPagerListner)

        add_photo_btn.setOnClickListener {
            clickListener?.isAdd(true)
        }
        add_photo_more_btn.setOnClickListener {
            clickListener?.isAdd(true)
        }
        remove_photo_btn.setOnClickListener {
            clickListener?.isAdd(false)
        }
    }

    fun setPhoto(post: Post){
        photoViewPagerAdapter.setId(post.id!!)

        photoList.add(Uri.parse(post.photo1))
        if(post.photo2 != null)  photoList.add(Uri.parse(post.photo2))
        if(post.photo3 != null)  photoList.add(Uri.parse(post.photo3))
        if(post.photo4 != null)  photoList.add(Uri.parse(post.photo4))
        photoViewPagerAdapter.notifyDataSetChanged()

        initIndicator(0)
        selectedNum = 0
    }

    private val viewPagerListner = object : ViewPager.OnPageChangeListener{
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
        photoList.add(uri)
        photoViewPagerAdapter.notifyDataSetChanged()

        selectedNum = photoList.size-1
        pager.setCurrentItem(selectedNum, true)
        initIndicator(selectedNum)
    }

    fun remove(){
        photoList.removeAt(selectedNum)
        photoViewPagerAdapter.notifyDataSetChanged()
        initIndicator(0)
        selectedNum = 0
    }

    private fun changeButton(){
        if(photoList.size > 0){
            add_photo_more_btn.visibility = View.VISIBLE
            remove_photo_btn.visibility = View.VISIBLE
            add_photo_btn.visibility = View.INVISIBLE
        }else{
            add_photo_more_btn.visibility = View.INVISIBLE
            remove_photo_btn.visibility = View.INVISIBLE
            add_photo_btn.visibility = View.VISIBLE
        }
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

        changeButton()
    }

    fun getSize(): Int {
        return if(!photoList.isNullOrEmpty()) photoList.size
        else 0
    }


}