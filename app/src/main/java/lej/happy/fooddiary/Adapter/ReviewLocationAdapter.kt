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
import lej.happy.fooddiary.Helper.ImageUtil
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

        var photoUri = countList!![position].post.photo
        holder!!.imageView.setClipToOutline(true)
        holder!!.imageView.setImageBitmap( ImageUtil.convert(photoUri))

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
                holder!!.rank2_img.setColorFilter(ContextCompat.getColor(context, R.color.brightGray));
                holder!!.rank2.setTextColor(ContextCompat.getColor(context, R.color.brightGray))
                holder!!.rank3_img.setColorFilter(ContextCompat.getColor(context, R.color.brightGray));
                holder!!.rank3.setTextColor(ContextCompat.getColor(context, R.color.brightGray))
            }
            2 -> {
                holder!!.rank2_img.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                holder!!.rank2.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                holder!!.rank1_img.setColorFilter(ContextCompat.getColor(context, R.color.brightGray));
                holder!!.rank1.setTextColor(ContextCompat.getColor(context, R.color.brightGray))
                holder!!.rank3_img.setColorFilter(ContextCompat.getColor(context, R.color.brightGray));
                holder!!.rank3.setTextColor(ContextCompat.getColor(context, R.color.brightGray))
            }
            3 -> {
                holder!!.rank1_img.setColorFilter(ContextCompat.getColor(context, R.color.brightGray));
                holder!!.rank1.setTextColor(ContextCompat.getColor(context, R.color.brightGray))
                holder!!.rank2_img.setColorFilter(ContextCompat.getColor(context, R.color.brightGray));
                holder!!.rank2.setTextColor(ContextCompat.getColor(context, R.color.brightGray))
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

}

