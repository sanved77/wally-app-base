package xyz.nagdibai.superwallpapers

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class FavApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { RoomDBFavs.getDatabase(this, applicationScope) }
    val repository by lazy { FavRepo(database.favDao()) }
}