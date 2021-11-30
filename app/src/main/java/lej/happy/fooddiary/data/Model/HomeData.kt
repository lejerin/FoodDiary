package lej.happy.fooddiary.data.model

import androidx.recyclerview.widget.GridLayoutManager
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.ui.adapter.PhotoGridAdapter

data class HomeData (

    //날짜, 새로운 달인지, 연도와 월, photo 리스트, photoAdapter 리스트

    val date: String,
    var isNew: Boolean = false,
    val yearAndMonth : String,
    var adapters: PhotoGridAdapter?,
    var layoutManager: GridLayoutManager?,
    var postList: MutableList<Post>
)