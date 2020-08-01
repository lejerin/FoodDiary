package lej.happy.fooddiary.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.Model.HomeString
import lej.happy.fooddiary.R
import org.w3c.dom.Text


class HomePhotoAdapter(
    timeList: MutableList<HomeString>,
    photoList: HashMap<String, MutableList<Post>>,
    gridAdapters: HashMap<String, PhotoGridAdapter>,
    isAll: Boolean
) : RecyclerView.Adapter<HomePhotoAdapter.TimePhotoViewHolder>() {

     var isAll: Boolean = isAll
    private var timeList : MutableList<HomeString> = timeList
    private var photoList : HashMap<String,MutableList<Post>> = photoList

    val gridadpters : HashMap<String, PhotoGridAdapter> = gridAdapters



    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimePhotoViewHolder {
        var view = LayoutInflater.from(parent!!.context).inflate(R.layout.row_item_home_time,parent,false)

        context = parent!!.context

        return TimePhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimePhotoViewHolder, position: Int) {

        var timeData = timeList!![position]

        val array: List<String> = timeData.date.split("-")

        if(isAll){
            if(timeData.isNew){
                holder!!.dateText.text = timeData.ym
                holder!!.dateText.visibility = View.VISIBLE
            }else{
                holder!!.dateText.visibility = View.GONE
            }
        }else{
            holder!!.dateText.visibility = View.GONE
        }

        holder!!.textTitle.text =  array[2]

//        if(gridadpters.containsKey(timeData.date)){
//            holder!!.rv_photo.adapter = gridadpters.get(timeData.date)
//        }else{
//            System.out.println("생성")
//
//            val gridadpters : HashMap<String, PhotoGridAdapter> = hashMapOf()
//            var photoAdapter = PhotoGridAdapter(photoList.get(timeData.date)!!)
//            val gridLayoutManager = GridLayoutManager(context, 3)
//
//            holder!!.rv_photo.adapter = photoAdapter
//            holder!!.rv_photo.layoutManager = gridLayoutManager
//            gridadpters.put(timeData.date, photoAdapter)
//        }

        holder!!.rv_photo.adapter = gridadpters.get(timeData.date)
        holder!!.rv_photo.layoutManager = GridLayoutManager(context, 3)

    }

    override fun getItemCount(): Int {
        return timeList!!.size
    }


    class TimePhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textTitle = view.findViewById(R.id.time_title) as Button
        var rv_photo = view.findViewById(R.id.rv_time_photo) as RecyclerView
        var dateText = view.findViewById(R.id.date_text) as TextView

    }
}

