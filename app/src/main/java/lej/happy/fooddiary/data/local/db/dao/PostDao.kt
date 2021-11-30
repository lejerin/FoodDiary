package lej.happy.fooddiary.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import lej.happy.fooddiary.data.local.db.entity.Post
import java.util.*

@Dao
interface PostDao :
    BaseDao<Post> {

    //해당 날짜에 저장된 ROW 수를 반환
    @Query("SELECT COUNT(date) FROM post WHERE date = :date")
    fun getCount(date: Date): Int

    //해당 날짜에 저장된 ROW 수를 반환
    @Query("SELECT * FROM post WHERE id = :postId")
    fun getPostWithId(postId: Long): Post

    @Query("SELECT * FROM post WHERE taste = :num ORDER BY date DESC Limit :sp, :ep")
    fun selectByTasteDesc(num: Int, sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post WHERE taste = :num ORDER BY date ASC Limit :sp, :ep")
    fun selectByTasteAsc(num: Int, sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post ORDER BY date DESC Limit :sp, :ep")
    fun selectByPageDesc(sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post ORDER BY date ASC Limit :sp, :ep")
    fun selectByPageAsc(sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post WHERE date BETWEEN :dayst AND :dayet ORDER BY date DESC Limit :sp, :ep")
    fun selectByDateDESC(dayst: Date , dayet: Date, sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post WHERE date BETWEEN :dayst AND :dayet ORDER BY date ASC Limit :sp, :ep")
    fun selectByDateASC(dayst: Date , dayet: Date, sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post WHERE address Not Null ORDER BY location DESC, date DESC Limit :sp, :ep")
    fun selectByLocationDesc(sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post WHERE address Not Null ORDER BY location ASC, date ASC Limit :sp, :ep")
    fun selectByLocationAsc(sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post WHERE address = :adr ORDER BY date DESC Limit :sp, :ep")
    fun selectByAddressDesc(adr: String, sp: Int, ep: Int): List<Post>

    @Query("SELECT * FROM post WHERE address = :adr ORDER BY date ASC Limit :sp, :ep")
    fun selectByAddressAsc(adr: String, sp: Int, ep: Int): List<Post>

    @Query("DELETE FROM post WHERE id = :id")
    fun deleteById(id : Long)



}
