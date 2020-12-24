package lej.happy.fooddiary.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import lej.happy.fooddiary.data.db.entity.Thumb

@Dao
interface ThumbDao :
    BaseDao<Thumb> {

    @Query("SELECT * FROM thumb WHERE id = :postId")
    fun selectById(postId: Long): Thumb


}
