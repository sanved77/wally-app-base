package com.example.myapplication

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import xyz.nagdibai.superwallpapers.R

const val LEFT = 101;
const val RIGHT = 102;

internal class CustomAdapter(private var itemsList: List<String>, private var context: Context) :
    RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemImageView: ImageView = view.findViewById(R.id.ivPopListItem)
        var popItemCard: CardView = view.findViewById(R.id.popItemCard)
        val param = popItemCard.layoutParams as ViewGroup.MarginLayoutParams
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemsList[position]
        when (position) {
            0 -> applyPadding(holder, LEFT)
            (itemCount - 1) -> applyPadding(holder, RIGHT)
        }
        Glide.with(context)
            .load(item)
            .into(holder.itemImageView)
    }
    override fun getItemCount(): Int {
        return itemsList.size
    }

    fun applyPadding(holder: MyViewHolder, side: Int) {
        var dpRatio = context.getResources().getDisplayMetrics().density;
        when (side) {
            LEFT -> holder.param.setMargins((12 * dpRatio).toInt(),0,(6 * dpRatio).toInt(),0)
            RIGHT -> holder.param.setMargins((6 * dpRatio).toInt(),0,(12 * dpRatio).toInt(),0)
        }
        holder.popItemCard.layoutParams = holder.param
    }
}