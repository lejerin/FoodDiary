package lej.happy.fooddiary.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.databinding.ItemRowGridPhotoBinding
import lej.happy.fooddiary.ui.main.MainActivity
import lej.happy.fooddiary.ui.view.ViewActivity
import lej.happy.fooddiary.utils.DiffUtilCallback
import lej.happy.fooddiary.utils.getActivity
import java.io.File

class PhotoGridAdapter(
    private val dataList: MutableList<Post>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val updateMutex = Mutex()
    // 리스트 갱신
    @SuppressLint("NotifyDataSetChanged")
    suspend fun updateList(pair: Pair<BaseValue.DATA_TYPE, List<Post>?>) {
        updateMutex.withLock {
            try {
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
                            diffResult.dispatchUpdatesTo(this@PhotoGridAdapter)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calDiffCallback(newData: List<Post>): DiffUtil.DiffResult {
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
        PhotoViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_row_grid_photo,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PhotoViewHolder)
        {
            val safePosition = holder.adapterPosition
            val data = dataList[safePosition]

            holder.binding.gridPhoto.apply {
                clipToOutline = true

                val file = File(data.photo1)
                Glide.with(this)
                    .load(data.photo1)
                    .signature(ObjectKey(file.path + file.lastModified()))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .thumbnail(0.1f) //실제 이미지 크기의 10%만 먼저 가져와서 흐릿하게 보여줍니다.
                    .into(this)

                setOnClickListener {
                    val detailPostIntent = Intent(context, ViewActivity::class.java)
                    detailPostIntent.putExtra("postId", data.id)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as MainActivity, this, "profile")
                    context.getActivity()?.startActivityForResult(detailPostIntent, BaseValue.ACTIVITY_RESULT_VIEW_POST,  options.toBundle())
                }
            }
        }
    }

    inner class PhotoViewHolder(
        val binding: ItemRowGridPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root)

}
