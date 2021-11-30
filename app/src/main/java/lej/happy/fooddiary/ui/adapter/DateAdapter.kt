package lej.happy.fooddiary.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.data.model.HomeData
import lej.happy.fooddiary.databinding.ItemRowDateBinding
import lej.happy.fooddiary.utils.DiffUtilCallback
import java.lang.IllegalArgumentException

class DateAdapter(
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val hashMapList: LinkedHashMap<String, HomeData> = linkedMapOf() // 순서 보장하는 LinkedHashMap
    private val dataList: MutableList<HomeData> = mutableListOf()
    var isAllMode = true

    // 리스트 갱신
    @SuppressLint("NotifyDataSetChanged")
    suspend fun updateList(pair: Pair<BaseValue.DATA_TYPE, LinkedHashMap<String, HomeData>?>) {
        Log.i("EUNJIN", "updateList size ${pair.second?.size}")
        val job = CoroutineScope(Dispatchers.IO).launch {
            val newList = pair.second
            if (newList != null) {
                val diffResult = when (pair.first) {
                    BaseValue.DATA_TYPE.ADD -> {
                        newList.values.forEach {
                            if (hashMapList.containsKey(it.date)) {
                                it.isNew = false
                                hashMapList[it.date]?.let { findData ->
                                    findData.postList.addAll(it.postList)
                                }
                            } else {
                                it.adapters = PhotoGridAdapter(it.postList)
                                hashMapList[it.date] = it
                            }
                        }
                        val addList = hashMapList.values.toList()
                        calDiffCallback(addList)
                    }
                    BaseValue.DATA_TYPE.INIT -> {
                        hashMapList.clear()
                        newList.values.forEach {
                            it.adapters = PhotoGridAdapter(it.postList)
                            hashMapList[it.date] = it
                        }
                        calDiffCallback(hashMapList.values.toList())
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    diffResult.dispatchUpdatesTo(this@DateAdapter)
                }
            } else { // 모두 지우기
                dataList.run {
                    clear()
                    CoroutineScope(Dispatchers.Main).launch {
                        notifyDataSetChanged()
                    }
                }
            }
        }
        job.join()
    }

    private fun calDiffCallback(newData: List<HomeData>): DiffUtil.DiffResult {
        val diffCallback = DiffUtilCallback(dataList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        dataList.run {
            clear()
            addAll(newData)
        }
        return diffResult
    }

    override fun getItemCount() = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DateViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_row_date,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateViewHolder)
        {
            val safePosition = holder.adapterPosition
            val posData = dataList[safePosition]

            val array: List<String> = posData.date.split("-")

            holder.binding.apply {
                tvDate.text = posData.yearAndMonth
                isDateShow = isAllMode && posData.isNew
                title.text = array[2]
                rvDatePhoto.adapter = posData.adapters
                if (rvDatePhoto.layoutManager == null) {
                    rvDatePhoto.layoutManager = GridLayoutManager(context, 3)
                }
            }
        }
    }

    inner class DateViewHolder(
        val binding: ItemRowDateBinding
    ) : RecyclerView.ViewHolder(binding.root)

}
