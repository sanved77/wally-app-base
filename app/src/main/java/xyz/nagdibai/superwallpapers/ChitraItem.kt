package xyz.nagdibai.superwallpapers

import java.io.Serializable

data class ChitraItem(
    val _id: String,
    val category: String,
    val subCategory: String,
    val downloads: Int,
    val keywords: String,
    val link: String
) : Serializable