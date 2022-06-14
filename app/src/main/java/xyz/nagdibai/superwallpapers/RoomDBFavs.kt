package xyz.nagdibai.superwallpapers

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(entities = [Favorite::class], version = 1, exportSchema = false)
public abstract class RoomDBFavs : RoomDatabase() {

    abstract fun favDao(): FavDAO

//    private class WordDatabaseCallback(
//        private val scope: CoroutineScope
//    ) : RoomDatabase.Callback() {
//
//        override fun onCreate(db: SupportSQLiteDatabase) {
//            super.onCreate(db)
//            INSTANCE?.let { database ->
//                scope.launch {
//                    var wordDao = database.favDao()
//                }
//            }
//        }
//    }


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