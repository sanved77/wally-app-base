package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import xyz.nagdibai.superwallpapers.CategoryListItem
import xyz.nagdibai.superwallpapers.LEFT
import xyz.nagdibai.superwallpapers.RIGHT
import xyz.nagdibai.superwallpapers.R


internal class CategoryListAdapter(private var itemsList: List<CategoryListItem>, private var context: Context, private var height: Int) :
    RecyclerView.Adapter<CategoryListAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemImageView: ImageView = view.findViewById(R.id.ivPopListItem)
        var popItemCard: CardView = view.findViewById(R.id.popItemCard)
        var tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val param = popItemCard.layoutParams as ViewGroup.MarginLayoutParams
        val width = (height/16)*9
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.cat_item_list, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemsList?.get(position)
        when (position) {
            0 -> applyPadding(holder, LEFT)
            (itemCount - 1) -> applyPadding(holder, RIGHT)
        }
        holder.popItemCard.getLayoutParams().height = height
        holder.popItemCard.getLayoutParams().width = holder.width
        Glide.with(context)
            .load(item.thumbLink)
            .into(holder.itemImageView)
        holder.tvLabel.text = item.label
    }
    override fun getItemCount(): Int {
        return itemsList?.size
    }

    fun applyPadding(holder: MyViewHolder, side: Int) {
        var dpRatio = context.getResources().getDisplayMetrics().density;
        when (side) {
            LEFT -> holder.param.setMargins((16 * dpRatio).toInt(),0,(4 * dpRatio).toInt(),0)
            RIGHT -> holder.param.setMargins((4 * dpRatio).toInt(),0,(8 * dpRatio).toInt(),0)
        }
        holder.popItemCard.layoutParams = holder.param
    }
}