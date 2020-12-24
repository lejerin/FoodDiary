package lej.happy.fooddiary.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import lej.happy.fooddiary.data.db.entity.Post
import java.util.*

@Dao
interface PostDao :
    BaseDao<Post> {

    //해당 날짜에 저장된 ROW 수를 반환
    @Query("SELECT COUNT(date) FROM post WHERE date = :date")
    fun getCount(date: Date): Int

    @Query("SELECT * FROM post WHERE taste = :num ORDER BY date DESC")
    fun selectByTasteDesc(num: Int): List<Post>

    @Query("SELECT * FROM post WHERE taste = :num ORDER BY date ASC")
    fun selectByTasteAsc(num: Int): List<Post>

    @Query("SELECT * FROM post ORDER BY date DESC Limit :sp, :ep")
    fun selectByPageDesc(sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post ORDER BY date ASC Limit :sp, :ep")
    fun selectByPageAsc(sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post WHERE date BETWEEN :dayst AND :dayet ORDER BY date DESC")
    fun selectByDateDESC(dayst: Date , dayet: Date): List<Post>

    @Query("SELECT * FROM post WHERE date BETWEEN :dayst AND :dayet ORDER BY date ASC")
    fun selectByDateASC(dayst: Date , dayet: Date): List<Post>

    @Query("SELECT * FROM post WHERE address Not Null ORDER BY location DESC, date DESC")
    fun selectByLocationDesc(): List<Post>

    @Query("SELECT * FROM post WHERE address = :adr ORDER BY date DESC")
    fun selectByAddressDesc(adr: String): List<Post>

    @Query("SELECT * FROM post WHERE address = :adr ORDER BY date ASC")
    fun selectByAddressAsc(adr: String): List<Post>

    @Query("DELETE FROM post WHERE id = :id")
    fun deleteById(id : Long)



}
