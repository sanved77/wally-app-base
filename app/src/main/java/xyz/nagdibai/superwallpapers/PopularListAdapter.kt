package com.example.myapplication

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xyz.nagdibai.superwallpapers.R
import xyz.nagdibai.superwallpapers.LEFT
import xyz.nagdibai.superwallpapers.RIGHT
import xyz.nagdibai.superwallpapers.applyPadding


internal class PopularListAdapter(private var itemsList: List<String>, private var context: Context, private var height: Int) :
    RecyclerView.Adapter<PopularListAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemImageView: ImageView = view.findViewById(R.id.ivPopListItem)
        var popItemCard: CardView = view.findViewById(R.id.popItemCard)
        val param = popItemCard.layoutParams as ViewGroup.MarginLayoutParams
        val width = (height/16)*9

    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemsList?.get(position)
        when (position) {
            0 -> applyPadding(context, holder.param, LEFT)
            (itemCount - 1) -> applyPadding(context, holder.param, RIGHT)
        }
        holder.popItemCard.getLayoutParams().height = height
        holder.popItemCard.getLayoutParams().width = holder.width
        Glide.with(context)
            .load(item)
            .into(holder.itemImageView)
    }
    override fun getItemCount(): Int {
        return itemsList?.size
    }
}