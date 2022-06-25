package xyz.nagdibai.superwallpapers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favs")
data class Favorite(
    @ColumnInfo(name = "_id") val _id : String,
    @ColumnInfo(name = "category") val category : String,
    @ColumnInfo(name = "subCategory") val subCategory : String,
    @ColumnInfo(name = "downloads") val downloads : Int,
    @ColumnInfo(name = "keywords") val keywords : String,
    @PrimaryKey @ColumnInfo(name = "link") val link : String
)
