package xyz.nagdibai.superwallpapers

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavDAO {
    @Query("SELECT * FROM favs")
    fun getAllFavsLive(): Flow<List<Favorite>>

    @Query("SELECT * FROM favs")
    fun getAllFavs(): List<Favorite>

    @Query("SELECT * FROM favs WHERE link = :link")
    fun getOneFav(link: String): Favorite

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(link: Favorite)

    @Query("DELETE FROM favs WHERE link = :link")
    suspend fun delete(link: String)

    @Query("DELETE FROM favs")
    suspend fun deleteAll()
}