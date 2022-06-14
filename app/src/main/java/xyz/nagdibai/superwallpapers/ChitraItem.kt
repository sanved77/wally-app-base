package xyz.nagdibai.superwallpapers

import java.io.Serializable

data class ChitraItem(
    val category: String,
    val downloads: Int,
    val keywords: String,
    val link: String
) : Serializable