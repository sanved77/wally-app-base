package xyz.nagdibai.superwallpapers

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide


internal class CategoryListAdapter(
    private var itemsList: List<ChitraItem>,
    private var categoryMap: HashMap<String, ArrayList<ChitraItem>>,
    private var context: Context,
    private var height: Int
) :
    RecyclerView.Adapter<CategoryListAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemImageView: ImageView = view.findViewById(R.id.ivPopListItem)
        var catItemCard: CardView = view.findViewById(R.id.catItemCard)
        var tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val param = catItemCard.layoutParams as ViewGroup.MarginLayoutParams
        val width = (height/16)*9
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.cat_item_list, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 60f
        circularProgressDrawable.start()
        val item = itemsList[position]
        when (position) {
            0 -> applyPadding(context, holder.param, LEFT)
            (itemCount - 1) -> applyPadding(context, holder.param, RIGHT)
        }
        holder.catItemCard.layoutParams.height = height
        holder.catItemCard.layoutParams.width = holder.width
        Glide.with(context)
            .load(item.link)
            .skipMemoryCache(true)
            .placeholder(circularProgressDrawable)
            .into(holder.itemImageView)

        holder.tvLabel.text = item.category
        holder.tvLabel.post(Runnable {
            val lineCount: Int = holder.tvLabel.lineCount
            if (lineCount >= 2) holder.tvLabel.textSize = 16f
        })
        holder.catItemCard.setOnClickListener {
            val intent = Intent(context, Shelf::class.java)
            intent.putExtra("CategoryLabel", item.category)
            intent.putExtra("Wallies", categoryMap[item.category])
            intent.putExtra("AllWallies", categoryMap["All"])
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return itemsList?.size
    }

}

