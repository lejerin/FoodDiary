package lej.happy.fooddiary.ui.time

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import lej.happy.fooddiary.Activity.ViewPostActivity
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.util.ImageUtil
import lej.happy.fooddiary.R
import lej.happy.fooddiary.ui.MainActivity
import lej.happy.fooddiary.util.getActivity


class PhotoGridAdapter(photoList: MutableList<Post>) : RecyclerView.Adapter<PhotoGridAdapter.PhotoViewHolder>() {

    private var photoList :  MutableList<Post> = photoList
    private lateinit var context: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {

        context = parent!!.context




        var view = LayoutInflater.from(parent!!.context).inflate(R.layout.row_item_grid_photo,parent,false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {

        var photoUri = photoList!![position].photo
        holder!!.imageView.setClipToOutline(true)
        holder!!.imageView.setImageBitmap( ImageUtil.convert(photoUri))

        holder!!.imageView.setOnClickListener {
            val detailPostIntent = Intent(context, ViewPostActivity::class.java)
            detailPostIntent.putExtra("post", photoList!![position])
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as MainActivity, holder!!.imageView, "profile")
            context.getActivity()?.startActivityForResult(detailPostIntent, 33,  options.toBundle())

        }
    }


    override fun getItemCount(): Int {
        return photoList!!.size
    }


    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView = view.findViewById(R.id.grid_photo) as ImageView

    }

}

