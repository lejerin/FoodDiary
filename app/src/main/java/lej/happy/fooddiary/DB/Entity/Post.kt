package lej.happy.fooddiary.DB.Entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "post")
data class Post(@PrimaryKey(autoGenerate = true) var id: Long?,
          @ColumnInfo(name = "date") var date: Date?,
          @ColumnInfo(name = "count") var count: Int,
          @ColumnInfo(name = "photo1") var photo1: String,
          @ColumnInfo(name = "photo2") var photo2: String?,
          @ColumnInfo(name = "photo3") var photo3: String?,
          @ColumnInfo(name = "photo4") var photo4: String?,
          @ColumnInfo(name = "texts") var texts: String?,
          @ColumnInfo(name = "time") var time: Int?,
          @ColumnInfo(name = "taste") var taste: Int,
          @ColumnInfo(name = "location") var location: String,
          @ColumnInfo(name = "address") var address: String?,
          @ColumnInfo(name = "x") var x: Double?,
          @ColumnInfo(name = "y") var y: Double?
) : Serializable {
    constructor(): this(null,null, 0,"", null, null, null,
        null, null, 0, "", null, null, null)
}
