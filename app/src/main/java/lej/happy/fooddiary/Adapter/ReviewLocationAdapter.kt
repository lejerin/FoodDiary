package lej.happy.fooddiary.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.Model.ReviewRank
import lej.happy.fooddiary.R


class ReviewLocationAdapter(rankCount: List<ReviewRank>) : RecyclerView.Adapter<ReviewLocationAdapter.PhotoViewHolder>() {

    private val countList :  List<ReviewRank> = rankCount
    private lateinit var context: Context

    //ClickListener
    interface OnItemClickListener {
        fun onClick(position: Int)
    }
    private lateinit var itemClickListener : OnItemClickListener

    fun setReviewItemClickListener(itemClickListener: OnItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {

        context = parent!!.context

        var view = LayoutInflater.from(parent!!.context).inflate(R.layout.row_item_review,parent,false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {

        holder!!.imageView.setClipToOutline(true);
        val getBitmap = decodeSampledBitmapFromResource(Uri.parse(countList!![position].post.photo1), 100, 100)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder!!.imageView.setImageBitmap(imgRotate(Uri.parse(countList!![position].post.photo1), getBitmap))
        }else{
            holder!!.imageView.setImageBitmap(getBitmap)
        }

        holder!!.title.text = countList!![position].post.location
        holder!!.address.text = countList!![position].post.address

        val best = countList!![position].best
        val good = countList!![position].good
        val bad = countList!![position].bad

        holder!!.num.text = "+" + (best + good + bad)

        val many = mutableMapOf<Int,Int>()
        many.put(bad, 3)
        many.put(good, 2)
        many.put(best, 1)

        when(many.get(Math.max(Math.max(best, good), bad))){
            1 -> {
                holder!!.rank1_img.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                holder!!.rank1.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            }
            2 -> {
                holder!!.rank2_img.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                holder!!.rank2.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            }
            3 -> {
                holder!!.rank3_img.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                holder!!.rank3.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            }
        }

        holder!!.rank1.text = "최고(" + best + ")"
        holder!!.rank2.text = "만족(" + good + ")"
        holder!!.rank3.text = "별로(" + bad + ")"

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(position)
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

    override fun getItemCount(): Int {
        return countList!!.size
    }


    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView = view.findViewById(R.id.review_rv_img) as ImageView
        var title = view.findViewById(R.id.review_rv_title) as TextView
        var address = view.findViewById(R.id.review_rv_address) as TextView
        var rank1 = view.findViewById(R.id.rv_review_rank1) as TextView
        var rank2 = view.findViewById(R.id.rv_review_rank2) as TextView
        var rank3 = view.findViewById(R.id.rv_review_rank3) as TextView
        var rank1_img = view.findViewById(R.id.rv_review_rank1_img) as ImageView
        var rank2_img = view.findViewById(R.id.rv_review_rank2_img) as ImageView
        var rank3_img = view.findViewById(R.id.rv_review_rank3_img) as ImageView
        var num = view.findViewById(R.id.review_rv_num_text) as Button
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

