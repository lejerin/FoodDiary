package lej.happy.fooddiary.data.local.repository

import android.content.Context
import lej.happy.fooddiary.data.local.db.AppDatabase

class PostRepos (val context: Context) {

    val db = AppDatabase.getInstance(context)

    fun getPostId(postId: Long) =
        db.postDao().getPostWithId(postId)

    fun getThumbId(postId : Long) =
        db.thumbDao().selectById(postId)

    fun deleteById(id: Long) =
        db.postDao().deleteById(id)
}