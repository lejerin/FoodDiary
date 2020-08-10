package lej.happy.fooddiary.Model

import androidx.recyclerview.widget.GridLayoutManager
import lej.happy.fooddiary.Adapter.PhotoGridAdapter
import lej.happy.fooddiary.DB.Entity.Post

data class LicenseItem (

    //날짜, 새로운 달인지, 연도와 월, photo 리스트, photoAdapter 리스트

    val title: String,
    val addr: String,
    val copy: String,
    val name: String

)