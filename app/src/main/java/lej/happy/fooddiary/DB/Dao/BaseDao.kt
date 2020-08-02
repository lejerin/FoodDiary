package lej.happy.fooddiary.DB.Dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(obj : T) : Long

    @Delete
    fun delete(obj : T)

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(obj : T)
}

