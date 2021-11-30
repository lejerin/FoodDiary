package lej.happy.fooddiary.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import lej.happy.fooddiary.data.local.db.entity.Thumb

@Dao
interface ThumbDao :
    BaseDao<Thumb> {

    @Query("SELECT * FROM thumb WHERE id = :postId")
    fun selectById(postId: Long): Thumb


}
