package lej.happy.fooddiary.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.item_photo_viewpager.view.*
import lej.happy.fooddiary.Activity.DetailViewPagerActivity
import lej.happy.fooddiary.R
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ViewPagerAdapter(private val list: ArrayList<Uri>): PagerAdapter() {

    private lateinit var context: Context

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        context = container!!.context

        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.item_photo_viewpager, container, false)

        view.ivItem.setClipToOutline(true)
        val getBitmap = decodeSampledBitmapFromResource(list[position], 300, 300)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.ivItem.setImageBitmap(imgRotate(list[position], getBitmap))
        }else{
            view.ivItem.setImageBitmap(getBitmap)
        }


        view.ivItem.setOnClickListener {
            val viewPagerIntent = Intent(context, DetailViewPagerActivity::class.java)
            viewPagerIntent.putExtra("pos",position)
            viewPagerIntent.putExtra("uri1",list[0].toString())
            if(list.size > 1)
                viewPagerIntent.putExtra("uri2",list[1].toString())
            if(list.size > 2)
                viewPagerIntent.putExtra("uri3",list[2].toString())
            if(list.size > 3)
                viewPagerIntent.putExtra("uri4",list[3].toString())

            context.startActivity(viewPagerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))


        }

        container.addView(view)
        return view
    }

    fun removeItem(position: Int) {
        if (position > -1 && position < list.size) {
            list.removeAt(position)
            notifyDataSetChanged()
        }
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

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun decodeSampledBitmapFromResource(
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {

        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {

            System.out.println(uri.toString())
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri),null, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri),null, this)!!

        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun imgRotate(uri: Uri, bitmap: Bitmap) : Bitmap{
        val ins = context.contentResolver.openInputStream(uri)
        val exif = ExifInterface(ins)
        ins?.close()

        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when(orientation){
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

}