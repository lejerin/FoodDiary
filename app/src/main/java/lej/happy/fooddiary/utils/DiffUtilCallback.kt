package lej.happy.fooddiary.utils

import androidx.recyclerview.widget.DiffUtil
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.data.model.HomeData

class DiffUtilCallback(
    private val oldData: List<Any>,
    private val newData: List<Any>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        try {
            val oldItem = oldData[oldItemPosition]
            val newItem = newData[newItemPosition]

            return when {
                oldItem is HomeData && newItem is HomeData -> {
                    oldItem.date == newItem.date && oldItem.postList.size == newItem.postList.size
                }
                oldItem is Post && newItem is Post -> {
                    oldItem.id == newItem.id
                }
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun getOldListSize(): Int = oldData.size

    override fun getNewListSize(): Int = newData.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        try {
            oldData[oldItemPosition] == newData[newItemPosition]
        } catch (e: IndexOutOfBoundsException) {
            false
        }
}