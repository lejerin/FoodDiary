package lej.happy.fooddiary.DB.Dao

import androidx.room.Dao
import androidx.room.Query
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.DB.Entity.Thumb
import java.util.*

@Dao
interface ThumbDao : BaseDao<Thumb> {

    @Query("SELECT * FROM thumb WHERE id = :postId")
    fun selectById(postId: Long): Thumb


}
