package xyz.nagdibai.superwallpapers

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.method.TextKeyListener.clear
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target


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
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 60f
        circularProgressDrawable.start()
        val item = itemsList?.get(position)
        applyPadding(context, holder.cardParam, if (position%2==0) LEFT else RIGHT, big = 8, small = 4, bottom = 8)
        holder.photoHolderParam.width = width
        holder.photoHolderParam.height = holder.height
        Glide.with(context)
            .load(item)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(circularProgressDrawable)
            .into(holder.itemImageView)
    }
    override fun getItemCount(): Int {
        return itemsList?.size
    }
}