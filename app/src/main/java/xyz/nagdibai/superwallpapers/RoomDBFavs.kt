package xyz.nagdibai.superwallpapers

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(
    entities = [
        Favorite::class
    ],
    version = 2,
    exportSchema = false
)
public abstract class RoomDBFavs : RoomDatabase() {

    abstract fun favDao(): FavDAO

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: RoomDBFavs? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): RoomDBFavs {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDBFavs::class.java,
                    "fav_db"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}