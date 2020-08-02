package lej.happy.fooddiary.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import lej.happy.fooddiary.DB.Dao.PostDao
import lej.happy.fooddiary.DB.Dao.ThumbDao
import lej.happy.fooddiary.DB.Entity.Post
import lej.happy.fooddiary.DB.Entity.Thumb


@Database(entities = [Post::class, Thumb::class], version = 1 , exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun thumbDao(): ThumbDao

    companion object {
        private val DB_NAME = "room-db"
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context)
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                }).build()
        }
    }

}