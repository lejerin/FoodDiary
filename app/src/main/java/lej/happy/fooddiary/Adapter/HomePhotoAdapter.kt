package lej.happy.fooddiary.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import lej.happy.fooddiary.Model.HomeData
import lej.happy.fooddiary.R


class HomePhotoAdapter(
    timeList: MutableList<HomeData>,
    isAll: Boolean
) : RecyclerView.Adapter<HomePhotoAdapter.TimePhotoViewHolder>() {

    var isAll: Boolean = isAll
    private var timeList : MutableList<HomeData> = timeList

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


        holder!!.rv_photo.adapter = timeData.adapters
        holder!!.rv_photo.layoutManager = timeData.layout

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

