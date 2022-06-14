package xyz.nagdibai.superwallpapers

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FavRepo (private val favDao: FavDAO) {

    val allFavs: Flow<List<Favorite>> = favDao.getAllFavsLive()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(link: Favorite) {
        favDao.insert(link)
    }
    suspend fun getOneFav(link: String) : Favorite {
        return withContext(Dispatchers.IO) {
            favDao.getOneFav(link)
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAllFavs() {
        favDao.getAllFavs()
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(link: String) {
        favDao.delete(link)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        favDao.deleteAll()
    }
}