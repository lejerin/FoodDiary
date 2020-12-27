package lej.happy.fooddiary.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "thumb")
data class Thumb(@PrimaryKey() var id: Long?,
                 @ColumnInfo(name = "mid_photo1") var photo1_bitmap: String?,
                 @ColumnInfo(name = "mid_photo2") var photo2_bitmap: String?,
                 @ColumnInfo(name = "mid_photo3") var photo3_bitmap: String?,
                 @ColumnInfo(name = "mid_photo4") var photo4_bitmap: String?
) : Serializable {
    constructor(): this(null, null, null, null, null)
}
