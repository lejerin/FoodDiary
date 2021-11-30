package lej.happy.fooddiary.ui.view

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import lej.happy.fooddiary.R
import lej.happy.fooddiary.data.BaseValue
import lej.happy.fooddiary.data.SingleLiveEvent
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.data.local.repository.PostRepos
import lej.happy.fooddiary.ui.base.BaseViewModel
import lej.happy.fooddiary.ui.map.MapDetailActivity
import lej.happy.fooddiary.ui.post.PostActivity
import lej.happy.fooddiary.utils.Coroutines
import lej.happy.fooddiary.utils.getActivity
import org.koin.java.KoinJavaComponent.inject


class ViewViewModel : BaseViewModel() {

    /** Repos */
    private val mRepos by inject(PostRepos::class.java)

    var isModified = false

    private val _finish = MutableLiveData<Boolean>()
    val finish : LiveData<Boolean>
        get() = _finish

    val post = SingleLiveEvent<Post>()

    fun getPostWithId(id: Long) {
        setJob(
            Coroutines.ioThenMain(
                {
                    mRepos.getPostId(id)
                },
                {
                    post.value = it
                }
            )
        )
    }

    fun showPopupMenuOrder(v: View){
        val context = v.context
        val popup = PopupMenu(context, v)
        popup.inflate(R.menu.post_item)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.modifyBtn -> {
                    val modifyIntent = Intent(context, PostActivity::class.java)
                    modifyIntent.putExtra("postId", post.value?.id)
                    context.getActivity()?.startActivityForResult(modifyIntent, BaseValue.REQUEST_CODE_MODIFY_POST)
                    true
                }
                R.id.deleteBtn -> {
                    showDeletePostDialog(v.context)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDeletePostDialog(context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setMessage("정말로 삭제하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.dialog_positive_text)) { dialog, id ->
                setDeleteData()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.dialog_negative_text)) { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun setDeleteData(){
        setJob(
            Coroutines.ioThenMain(
                {  mRepos.deleteById(post.value?.id!!) },
                {
                    isModified = true
                    _finish.value = true
                }
            ))
    }

    fun goMapActivity(v: View) {
        val postValue = post.value
        if (postValue?.address != null){
            val context = v.context
            val mapDetailIntent = Intent(context, MapDetailActivity::class.java)
            mapDetailIntent.putExtra("x", postValue.x)
            mapDetailIntent.putExtra("y", postValue.y)
            mapDetailIntent.putExtra("name", postValue.location)
            mapDetailIntent.putExtra("address", postValue.address)
            context.getActivity()?.startActivity(mapDetailIntent)
        }
    }

    fun timeString(): String{
        var str = ""
        when(post.value?.time){
            1 -> str = "아침"
            2 -> str = "점심"
            3 -> str = "저녁"
            4 -> str = "야식"
            5 -> str = "간식"
        }
        return str
    }

    fun tasteString(): String{
        var str = ""
        when (post.value?.taste) {
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
        return str
    }

    fun getTasteImg(taste: Int): Int {
        when (taste) {
            1 ->  return R.drawable.laughing
            2 ->  return R.drawable.happy
            3 ->  return R.drawable.nervous
        }
        return R.drawable.laughing
    }
}