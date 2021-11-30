package lej.happy.fooddiary.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.remote.model.Document
import lej.happy.fooddiary.databinding.ItemRowMapListBinding


class MapListAdapter(
    private val dataList: MutableList<Document>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    //ClickListener
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    private lateinit var itemClickListener : OnItemClickListener

    fun setItemClickListener(itemClickListener: OnItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MapDataViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_row_map_list,
                parent,
                false
            )
        )

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MapDataViewHolder)
        {
            val safePosition = holder.adapterPosition
            val posData = dataList[safePosition]

            holder.binding.data = posData

            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, position)
            }
        }
    }

    inner class MapDataViewHolder(
        val binding: ItemRowMapListBinding
    ) : RecyclerView.ViewHolder(binding.root)

}

