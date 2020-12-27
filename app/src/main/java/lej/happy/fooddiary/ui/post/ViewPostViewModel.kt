package lej.happy.fooddiary.ui.post

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.activity_detail_post.*
import kotlinx.coroutines.*
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.Model.ReviewRank
import lej.happy.fooddiary.data.Repository
import lej.happy.fooddiary.data.db.AppDatabase
import lej.happy.fooddiary.data.db.entity.Post
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.ui.map.MapDetailActivity
import lej.happy.fooddiary.util.Coroutines
import lej.happy.fooddiary.util.getActivity
import java.text.SimpleDateFormat
import java.util.*

class ViewPostViewModel(
) : BaseViewModel() {

    lateinit var repository: Repository

    lateinit var post: Post

    var isModified = false
    private val REQUEST_CODE_MODIFY_POST = 66

    fun showPopupMenuOrder(v: View){
        val context = v.context

        val popup = PopupMenu(context, v)
        popup.inflate(R.menu.post_item)
        //adding click listener
        popup.setOnMenuItemClickListener { item ->

            when (item.itemId) {
                R.id.modifyBtn -> {

                    val modifyIntent = Intent(context, AddPostActivity::class.java)
                    modifyIntent.putExtra("post", post)
                    context.getActivity()?.startActivityForResult(modifyIntent, REQUEST_CODE_MODIFY_POST)

                    true
                }
                R.id.deleteBtn -> {
                    showDeletePostDialog(v)

                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDeletePostDialog(v: View){
        val builder = AlertDialog.Builder(v.context)
        builder.setMessage("정말로 삭제하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                setDeleteData(v)
            }
            .setNegativeButton("No") { dialog, id ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private val _finish = MutableLiveData<Boolean>()
    val finish : LiveData<Boolean>
        get() = _finish

    private fun setDeleteData(v: View){

        newJob(
            Coroutines.ioThenMain(
            {  repository.deleteById(post.id!!) },
            {
                _finish.value = true
            }
        ))

    }

    fun goMapActivity(v: View){
        if(post.address != null){
            val context = v.context
            val mapDetailIntent = Intent(context, MapDetailActivity::class.java)
            mapDetailIntent.putExtra("x", post.x)
            mapDetailIntent.putExtra("y", post.y)
            mapDetailIntent.putExtra("name", post.location)
            mapDetailIntent.putExtra("address", post.address)
            context.getActivity()?.startActivity(mapDetailIntent)
        }
    }

    fun getDateString(): String =  SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(post.date)

    fun timeString(): String{
        var str = ""
        if(post.time != null){
            when(post.time){
                1 -> str = "아침"
                2 -> str = "점심"
                3 -> str = "저녁"
                4 -> str = "야식"
                5 -> str = "간식"
            }
        }
        return str
    }

    fun tasteString(): String{
        var str = ""

        if(post.taste != null){
            when(post.taste){
                1 -> {
                    str = "최고"
                }
                2 -> {
                    str = "만족"
                }
                3 -> {
                    str = "별로"
                }
            }
        }
        return str
    }

    fun getTasteImg(): Int {
        if(post.taste != null){
            when(post.taste){
                1 ->  return R.drawable.laughing
                2 ->  return R.drawable.happy
                3 ->  return R.drawable.nervous
            }
        }
        return R.drawable.laughing
    }

}