package lej.happy.fooddiary.Adapter

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_photo_viewpager.view.*
import lej.happy.fooddiary.Activity.MainActivity
import lej.happy.fooddiary.Activity.ViewPostActivity
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.R


class PhotoGridAdapter(photoList: MutableList<Post>) : RecyclerView.Adapter<PhotoGridAdapter.PhotoViewHolder>() {

    private var photoList :  MutableList<Post> = photoList
    private lateinit var context: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {

        context = parent!!.context

        var view = LayoutInflater.from(parent!!.context).inflate(R.layout.row_item_grid_photo,parent,false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {

        var photoUri = photoList!![position].photo1
        holder!!.imageView.setClipToOutline(true)
        val getBitmap = decodeSampledBitmapFromResource(Uri.parse(photoUri), 100, 100)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder!!.imageView.setImageBitmap(imgRotate(Uri.parse(photoUri), getBitmap))
        }else{
            holder!!.imageView.setImageBitmap(getBitmap)
        }


        holder!!.imageView.setOnClickListener {
            val detailPostIntent = Intent(context, ViewPostActivity::class.java)
            detailPostIntent.putExtra("post", photoList!![position])
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as MainActivity, holder!!.imageView, "profile")
            context.startActivity(detailPostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), options.toBundle())

        }
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

            inJustDecodeBounds = true
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri),null, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri),null, this)!!
        }
    }

    override fun getItemCount(): Int {
        return photoList!!.size
    }


    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView = view.findViewById(R.id.grid_photo) as ImageView

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

