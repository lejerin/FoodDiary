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
import lej.happy.fooddiary.R
import org.w3c.dom.Text


class HomePhotoAdapter(
    timeList: MutableList<String>,
    photoList: HashMap<String, MutableList<Post>>,
    isAll: Boolean
) : RecyclerView.Adapter<HomePhotoAdapter.TimePhotoViewHolder>() {

    public var isAll: Boolean = isAll
    private var timeList : MutableList<String> = timeList
    private var photoList : HashMap<String,MutableList<Post>> = photoList
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimePhotoViewHolder {
        var view = LayoutInflater.from(parent!!.context).inflate(R.layout.row_item_home_time,parent,false)

        context = parent!!.context
        return TimePhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimePhotoViewHolder, position: Int) {

        var timeData = timeList!![position]

        val array: List<String> = timeData.split("-")
        System.out.println("isall" + isAll)
        if(isAll){
            val nowMonth = array[0] + "-" + array[1]
            if(position == 0){
                holder!!.dateText.text = nowMonth
                holder!!.dateText.visibility = View.VISIBLE
            }else{
                val bearray: List<String> = timeList!![position-1].split("-")
                val before = bearray[0] + "-" + bearray[1]
                if(nowMonth.equals(before)){
                    holder!!.dateText.visibility = View.GONE
                }else{
                    holder!!.dateText.text = nowMonth
                    holder!!.dateText.visibility = View.VISIBLE
                }
            }
        }else{
            holder!!.dateText.visibility = View.GONE
        }


        holder!!.textTitle.text =  array[2]

        if(photoList.get(timeData) != null){
            var photoAdapter = PhotoGridAdapter(0, photoList.get(timeData)!!)
            val gridLayoutManager = GridLayoutManager(context, 3)

            holder!!.rv_photo.adapter = photoAdapter
            holder!!.rv_photo.layoutManager = gridLayoutManager
        }



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

