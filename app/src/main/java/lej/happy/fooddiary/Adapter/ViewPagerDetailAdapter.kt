package lej.happy.fooddiary.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.item_photo_detail_viewpager.view.*
import kotlinx.android.synthetic.main.item_photo_viewpager.view.ivItem
import kotlinx.coroutines.*
import lej.happy.fooddiary.DB.AppDatabase
import lej.happy.fooddiary.DB.Entity.Thumb
import lej.happy.fooddiary.Helper.ImageUtil
import lej.happy.fooddiary.R
import java.io.InputStream
import kotlin.collections.ArrayList

class ViewPagerDetailAdapter(private val list: ArrayList<Uri> , private var id: Long): PagerAdapter() {

    private val bitmapList = mutableListOf<String>()
    private lateinit var context: Context

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        context = container!!.context

        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.item_photo_detail_viewpager, container, false)


        if(!checkUriValid(list[position])){
            view.warning_viewpager_text.visibility = View.VISIBLE

            if (bitmapList.size > 0) {
                view.ivItem.setImageBitmap(ImageUtil.convert(bitmapList[position]))
            } else {
                CoroutineScope(Job() + Dispatchers.Main).launch(Dispatchers.Default) {
                    val result = async {
                        getDataInDb() // some background work
                    }.await()
                    withContext(Dispatchers.Main) {
                        // some UI thread work for when the background work is done
                        setBitmapList(result)
                    }
                }
            }
        }else{
            view.ivItem.setImageURI(list[position])
        }

        container.addView(view)
        return view
    }

    fun checkUriValid(uri: Uri) : Boolean{
        var ls : InputStream? = null
        try {
            ls = context.getContentResolver().openInputStream(uri)
        }catch (e: Exception){

        }

        if(ls == null)
            return false

        return true
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

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE;
    }

    private fun getDataInDb(): Thumb {

        val getDb = AppDatabase.getInstance(context!!)

        return getDb.thumbDao().selectById(id)
    }

    private fun setBitmapList(thumb: Thumb) {

        if (thumb != null) {
            bitmapList.add(thumb.photo1_bitmap!!)
            if (thumb.photo2_bitmap != null) bitmapList.add(thumb.photo2_bitmap!!)
            if (thumb.photo3_bitmap != null) bitmapList.add(thumb.photo3_bitmap!!)
            if (thumb.photo4_bitmap != null) bitmapList.add(thumb.photo4_bitmap!!)
            System.out.println("뷰페이저 갱신함")
            notifyDataSetChanged()
        }

    }
}