package xyz.nagdibai.superwallpapers

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


internal class PopularListAdapter(private var itemsList: List<ChitraItem>, private var context: Context, private var height: Int) :
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
            .inflate(R.layout.popular_item, parent, false)
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
            .load(item.link)
            .skipMemoryCache(true)
            .into(holder.itemImageView)

        holder.itemImageView.setOnClickListener {
            val intent = Intent(context, PhotoWindow::class.java)
            intent.putExtra("category", item.category)
            intent.putExtra("downloads", item.downloads)
            intent.putExtra("link", item.link)
            intent.putExtra("keywords", item.keywords)
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return itemsList?.size
    }
}