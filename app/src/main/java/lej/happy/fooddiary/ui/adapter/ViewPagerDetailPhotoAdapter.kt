package lej.happy.fooddiary.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_photo_viewpager.view.*
import kotlinx.coroutines.*
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.local.db.AppDatabase
import lej.happy.fooddiary.data.local.db.entity.Thumb
import lej.happy.fooddiary.utils.CameraUtils
import lej.happy.fooddiary.utils.ImageUtils
import org.koin.java.KoinJavaComponent.inject
import java.io.FileNotFoundException

class ViewPagerDetailPhotoAdapter(private val list: MutableList<String>, private var id: Long): PagerAdapter() {

    private val cameraUtils: CameraUtils by inject(CameraUtils::class.java)

    private lateinit var context: Context
    private val bitmapList  = mutableListOf<String>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        context = container.context

        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.item_photo_detail_viewpager, container, false)

        try {
            if (!cameraUtils.checkUriValid(list[position])) {
                view.findViewById<TextView>(R.id.warning_viewpager_text).visibility = View.VISIBLE
                if (bitmapList.size > position) {
                    view.ivItem.setImageBitmap(ImageUtils.convert(bitmapList[position]))
                } else {
                    throw FileNotFoundException("ERROR")
                }
            } else {
                Glide.with(context)
                    .load(list[position])
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(view.ivItem)
            }
        } catch (err: FileNotFoundException) {
            try {
                if (bitmapList.isEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        setBitmapList(getDataInDb())
                        CoroutineScope(Dispatchers.Main).launch {
                            view.ivItem.setImageBitmap(ImageUtils.convert(bitmapList[position]))
                            notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        container.addView(view)
        return view
    }

    private fun getDataInDb() : Thumb? {
        val getDb = AppDatabase.getInstance(context)
        return getDb.thumbDao().selectById(id)
    }

    private fun setBitmapList(thumb: Thumb?){
        thumb?.let {
            bitmapList.add(thumb.photo1_bitmap!!)
            if(thumb.photo2_bitmap != null) bitmapList.add(thumb.photo2_bitmap!!)
            if(thumb.photo3_bitmap != null) bitmapList.add(thumb.photo3_bitmap!!)
            if(thumb.photo4_bitmap != null) bitmapList.add(thumb.photo4_bitmap!!)
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View?)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE;
    }

    override fun getCount(): Int {
        return list.size
    }
}