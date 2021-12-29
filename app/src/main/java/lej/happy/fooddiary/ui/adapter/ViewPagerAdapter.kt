package lej.happy.fooddiary.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.item_photo_viewpager.view.*
import kotlinx.coroutines.*
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.local.db.AppDatabase
import lej.happy.fooddiary.data.local.db.entity.Thumb
import lej.happy.fooddiary.ui.view.ViewPhotoActivity
import lej.happy.fooddiary.utils.ImageUtils
import java.io.File
import java.io.FileNotFoundException

class ViewPagerAdapter(private val list: MutableList<String>, private var id: Long): PagerAdapter() {

    private lateinit var context: Context
    private val bitmapList  = mutableListOf<String>()

    fun setId(newId: Long){
        id = newId
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        context = container.context

        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.item_photo_viewpager, container, false)

        view.ivItem.clipToOutline = true

        try {
            val file = File(list[position])
            Glide.with(context)
                .load(list[position])
                .signature(ObjectKey(file.path + file.lastModified()))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .thumbnail(0.3f) //실제 이미지 크기의 10%만 먼저 가져와서 흐릿하게 보여줍니다.
                .into(view.ivItem)
        } catch (err: FileNotFoundException) {
            if (bitmapList.isEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    setBitmapList(getDataInDb())
                    CoroutineScope(Dispatchers.Main).launch {
                        view.ivItem.setImageBitmap(ImageUtils.convert(bitmapList[position]))
                        notifyDataSetChanged()
                    }
                }
            }
        }

        view.ivItem.setOnClickListener {
            val viewPagerIntent = Intent(context, ViewPhotoActivity::class.java)
            viewPagerIntent.putExtra("pos",position)
            viewPagerIntent.putExtra("id",id)
            viewPagerIntent.putExtra("uri1",list[0])
            if(list.size > 1)
                viewPagerIntent.putExtra("uri2",list[1])
            if(list.size > 2)
                viewPagerIntent.putExtra("uri3",list[2])
            if(list.size > 3)
                viewPagerIntent.putExtra("uri4",list[3])

            context.startActivity(viewPagerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

        }
        container.addView(view)
        return view
    }

    private fun getDataInDb() : Thumb? {
        val getDb = AppDatabase.getInstance(context)
        return getDb.thumbDao().selectById(id)
    }

    private fun setBitmapList(thumb: Thumb?) {
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