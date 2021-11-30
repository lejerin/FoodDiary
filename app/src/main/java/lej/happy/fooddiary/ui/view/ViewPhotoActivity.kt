package lej.happy.fooddiary.ui.view

import androidx.viewpager.widget.ViewPager
import lej.happy.fooddiary.R
import lej.happy.fooddiary.databinding.ActivityViewPhotoBinding
import lej.happy.fooddiary.ui.adapter.ViewPagerDetailPhotoAdapter
import lej.happy.fooddiary.ui.base.BaseActivity
import lej.happy.fooddiary.ui.post.PostActivity

class ViewPhotoActivity : BaseActivity<ActivityViewPhotoBinding>() {
    private val TAG = PostActivity::class.java.simpleName

    override val layoutResourceId: Int
        get() = R.layout.activity_view_photo

    private val photoList = mutableListOf<String>()

    private val viewpager by lazy { binding.viewpager }
    private val circleAnimIndicator by lazy { binding.circleAnimIndicator }

    override fun initStartView() {
        initView()
    }

    private fun initView() {
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                circleAnimIndicator.selectDot(position)
            }

        })

        intent.getStringExtra("uri1")?.let { photoList.add(it) }
        intent.getStringExtra("uri2")?.let { photoList.add(it) }
        intent.getStringExtra("uri3")?.let { photoList.add(it) }
        intent.getStringExtra("uri4")?.let { photoList.add(it) }

        val id = intent.getLongExtra("id", -1)
        val photoViewPagerAdapter =
            ViewPagerDetailPhotoAdapter(
                photoList,
                id
            )
        viewpager.adapter = photoViewPagerAdapter

        //원사이의 간격
        circleAnimIndicator.setItemMargin(15)
        //애니메이션 속도
        circleAnimIndicator.setAnimDuration(300)
        //indecator 생성
        circleAnimIndicator.createDotPanel(photoList.size, R.drawable.viewpage_indicator_off , R.drawable.viewpager_indicator_on)

        viewpager.setCurrentItem(intent.getIntExtra("pos",0), false)
    }

    override fun afterPermission() {

    }
}