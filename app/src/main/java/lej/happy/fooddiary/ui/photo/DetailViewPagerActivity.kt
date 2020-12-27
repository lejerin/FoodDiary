package lej.happy.fooddiary.ui.photo


import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_view_pager.*

import lej.happy.fooddiary.R

class DetailViewPagerActivity : AppCompatActivity() {

    val photoList = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager)


        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                circleAnimIndicator.selectDot(position)
            }

        })

        photoList.add(Uri.parse(intent.getStringExtra("uri1")))
        if (intent.getStringExtra("uri2") != null) {
            photoList.add(Uri.parse(intent.getStringExtra("uri2")))
        }
        if (intent.getStringExtra("uri3") != null) {
            photoList.add(Uri.parse(intent.getStringExtra("uri3")))
        }
        if (intent.getStringExtra("uri4") != null) {
            photoList.add(Uri.parse(intent.getStringExtra("uri4")))
        }


        val id = intent.getLongExtra("id", 0)
        val photoViewPagerAdapter =
            ViewPagerDetailAdapter(
                photoList as ArrayList<Uri>,
                id
            )
        viewpager.adapter = photoViewPagerAdapter


        //원사이의 간격
        circleAnimIndicator.setItemMargin(15);
        //애니메이션 속도
        circleAnimIndicator.setAnimDuration(300);
        //indecator 생성
        circleAnimIndicator.createDotPanel(photoList.size, R.drawable.viewpage_indicator_off , R.drawable.viewpager_indicator_on);

        viewpager.setCurrentItem(intent.getIntExtra("pos",0), false)

    }
}