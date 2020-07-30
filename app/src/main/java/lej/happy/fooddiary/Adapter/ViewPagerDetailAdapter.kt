package lej.happy.fooddiary.Adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.item_photo_viewpager.view.ivItem
import lej.happy.fooddiary.R
import kotlin.collections.ArrayList

class ViewPagerDetailAdapter(private val list: ArrayList<Uri>): PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.item_photo_detail_viewpager, container, false)


        view.ivItem.setImageURI(list[position])


        container.addView(view)
        return view
    }


    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View?)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {

        return view == obj
    }

    override fun getCount(): Int {
        return list.size
    }
}