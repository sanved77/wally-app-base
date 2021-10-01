package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import xyz.nagdibai.superwallpapers.R
import xyz.nagdibai.superwallpapers.LEFT
import xyz.nagdibai.superwallpapers.RIGHT
import xyz.nagdibai.superwallpapers.applyPadding


internal class PhotoShelfAdapter(private var itemsList: ArrayList<String>, private var context: Context, private var width: Int) :
    RecyclerView.Adapter<PhotoShelfAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemImageView: ImageView = view.findViewById(R.id.ivPopListItem)
        var popItemCard: CardView = view.findViewById(R.id.popItemCard)
        var llPhotoHolder: LinearLayout = view.findViewById(R.id.llPhotoHolder)
        val photoHolderParam = llPhotoHolder.layoutParams as ViewGroup.MarginLayoutParams
        val cardParam = popItemCard.layoutParams as ViewGroup.MarginLayoutParams
        val height = (width/9)*16

    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.photo_item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemsList?.get(position)
        applyPadding(context, holder.cardParam, if (position%2==0) LEFT else RIGHT, small = 8, bottom = 16)
        holder.photoHolderParam.width = width
        holder.photoHolderParam.height = holder.height
        Glide.with(context)
            .load(item)
            .into(holder.itemImageView)
    }
    override fun getItemCount(): Int {
        return itemsList?.size
    }
}