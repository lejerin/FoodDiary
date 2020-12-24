package lej.happy.fooddiary.Model

import androidx.recyclerview.widget.GridLayoutManager
import lej.happy.fooddiary.Adapter.PhotoGridAdapter

data class HomeData (

    //날짜, 새로운 달인지, 연도와 월, photo 리스트, photoAdapter 리스트

    val date: String,
    val isNew: Boolean = false,
    val ym : String,
    var adapters: PhotoGridAdapter?,
    var layout: GridLayoutManager?

)