package lej.happy.fooddiary.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import lej.happy.fooddiary.data.local.db.dao.PostDao
import lej.happy.fooddiary.data.local.db.dao.ThumbDao
import lej.happy.fooddiary.data.local.db.entity.Post
import lej.happy.fooddiary.data.local.db.entity.Thumb


@Database(entities = [Post::class, Thumb::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun thumbDao(): ThumbDao

    companion object {
        private val DB_NAME = "room-db"
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance
                    ?: buildDatabase(
                        context
                    )
            }
        }

        private val MIGRATION_1_2 = object : Migration(1,2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    execSQL("CREATE TABLE post_Backup('id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "'date' INTEGER NULL," +
                            "'count' INTEGER NOT NULL," +
                            "'photo1' TEXT NOT NULL," +
                            "'photo2' TEXT NULL," +
                            "'photo3' TEXT NULL," +
                            "'photo4' TEXT NULL," +
                            "'texts' TEXT NULL," +
                            "'time' INTEGER NULL," +
                            "'taste' INTEGER NULL," +
                            "'location' TEXT NULL," +
                            "'address' TEXT NULL," +
                            "'x' DOUBLE NULL," +
                            "'y' DOUBLE NULL);")
                    execSQL("INSERT INTO post_Backup SELECT id, date, count, photo1, photo2, photo3, photo4, texts, time, taste, location, address, x, y FROM post")
                    execSQL("DROP TABLE post")
                    execSQL("ALTER TABLE post_Backup RENAME to post")
                }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                }).build()
        }
    }

}