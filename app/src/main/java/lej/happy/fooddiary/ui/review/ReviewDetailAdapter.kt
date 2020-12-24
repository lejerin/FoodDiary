package lej.happy.fooddiary.ui.review

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import lej.happy.fooddiary.ui.time.PhotoGridAdapter
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.R


class ReviewDetailAdapter(
    timeList: MutableList<String>,
    photoList: HashMap<String, MutableList<Post>>
) : RecyclerView.Adapter<ReviewDetailAdapter.TimePhotoViewHolder>() {

    private var timeList : MutableList<String> = timeList
    private var photoList : HashMap<String,MutableList<Post>> = photoList
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimePhotoViewHolder {
        var view = LayoutInflater.from(parent!!.context).inflate(R.layout.row_item_review_photo,parent,false)

        context = parent!!.context
        return TimePhotoViewHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: TimePhotoViewHolder, position: Int) {

        var timeData = timeList!![position]

        if(position % 2 == 0){
            holder!!.textTitle.text =  timeData
            if(photoList.get(timeData) != null){
                var photoAdapter = PhotoGridAdapter(
                    photoList.get(timeData)!!
                )
                val gridLayoutManager = GridLayoutManager(context, 3)

                holder!!.rv_photo.adapter = photoAdapter
                holder!!.rv_photo.layoutManager = gridLayoutManager
            }
            holder!!.textTitle.visibility = View.VISIBLE
            holder!!.rv_photo.visibility = View.VISIBLE
        }else{
            holder!!.textTitle2.text =  timeData
            if(photoList.get(timeData) != null){
                var photoAdapter = PhotoGridAdapter(
                    photoList.get(timeData)!!
                )
                val gridLayoutManager = object : GridLayoutManager(context, 3) {
                    override fun isLayoutRTL(): Boolean {
                        return true
                    }
                }
                holder!!.rv_photo2.adapter = photoAdapter
                holder!!.rv_photo2.layoutManager = gridLayoutManager
            }
            holder!!.textTitle2.visibility = View.VISIBLE
            holder!!.rv_photo2.visibility = View.VISIBLE
        }





    }

    override fun getItemCount(): Int {
        return timeList!!.size
    }



    class TimePhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textTitle = view.findViewById(R.id.time_title) as Button
        var rv_photo = view.findViewById(R.id.rv_review_photo) as RecyclerView

        var textTitle2 = view.findViewById(R.id.time_title2) as Button
        var rv_photo2 = view.findViewById(R.id.rv_review_photo2) as RecyclerView

    }
}

