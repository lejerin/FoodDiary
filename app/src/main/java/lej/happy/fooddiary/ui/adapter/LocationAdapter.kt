package lej.happy.fooddiary.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.model.ReviewRank
import lej.happy.fooddiary.databinding.ItemRowLocationBinding
import lej.happy.fooddiary.ui.location.detail.LocationDetailFragment
import lej.happy.fooddiary.utils.DiffUtilCallback
import java.io.File

class LocationAdapter(
    private val context: Context,
    private val action: (Fragment) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var dataList: MutableList<ReviewRank> = mutableListOf()

    // 리스트 갱신
    @SuppressLint("NotifyDataSetChanged")
    suspend fun updateList(pair: Pair<BaseValue.DATA_TYPE, List<ReviewRank>?>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            val newList = pair.second
            if (newList != null) {
                val diffResult = when (pair.first) {
                    BaseValue.DATA_TYPE.ADD -> {
                        val addList = dataList.toMutableList().apply {
                            addAll(newList)
                        }
                        calDiffCallback(addList)
                    }
                    BaseValue.DATA_TYPE.INIT -> {
                        calDiffCallback(newList)
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    diffResult.dispatchUpdatesTo(this@LocationAdapter)
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

    private fun calDiffCallback(newData: List<ReviewRank>): DiffUtil.DiffResult {
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
                R.layout.item_row_location,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateViewHolder)
        {
            val safePosition = holder.adapterPosition
            val posData = dataList[safePosition]

            holder.binding.apply {

                root.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("address", posData.post.address)
                    bundle.putString("name", posData.post.location)
                    action.invoke(LocationDetailFragment().apply { arguments = bundle })
                }

                reviewRvImg.apply {
                    clipToOutline = true
                    val photo = posData.post.photo1
                    val file = File(photo)
                    Glide.with(this)
                        .load(photo)
                        .signature(ObjectKey(file.path + file.lastModified()))
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .centerCrop()
                        .thumbnail(0.1f) //실제 이미지 크기의 10%만 먼저 가져와서 흐릿하게 보여줍니다.
                        .into(this)
                }

                reviewRvTitle.text = posData.post.location
                reviewRvAddress.text = posData.post.address

                val best = posData.best
                val good = posData.good
                val bad = posData.bad

                reviewRvNumText.text = "+" + posData.num

                rvReviewRank1.text = "최고(" + best + ")"
                rvReviewRank2.text = "만족(" + good + ")"
                rvReviewRank3.text = "별로(" + bad + ")"

                val many = mutableMapOf<Int,Int>()
                many[bad] = 3
                many[good] = 2
                many[best] = 1

                val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
                val grayColor = ContextCompat.getColor(context, R.color.brightGray)

                val primaryPos = many[best.coerceAtLeast(good).coerceAtLeast(bad)]

                rvReviewRank1Img.setColorFilter(if (primaryPos == 1) primaryColor else grayColor)
                rvReviewRank1.setTextColor(if (primaryPos == 1) primaryColor else grayColor)
                rvReviewRank2Img.setColorFilter(if (primaryPos == 2) primaryColor else grayColor)
                rvReviewRank2.setTextColor(if (primaryPos == 2) primaryColor else grayColor)
                rvReviewRank3Img.setColorFilter(if (primaryPos == 3) primaryColor else grayColor)
                rvReviewRank3.setTextColor(if (primaryPos == 3) primaryColor else grayColor)
            }
        }
    }

    inner class DateViewHolder(
        val binding: ItemRowLocationBinding
    ) : RecyclerView.ViewHolder(binding.root)

}