package xyz.nagdibai.superwallpapers

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide


internal class CategoryShelfAdapter(
    private var itemsList: List<ChitraItem>,
    private var subCategoryMap: HashMap<String, ArrayList<ChitraItem>>,
    private var allPhotoList: ArrayList<ChitraItem>,
    private var context: Context,
    private var width: Int
) :
    RecyclerView.Adapter<CategoryShelfAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemImageView: ImageView = view.findViewById(R.id.ivPopListItem)
        var catItemCard: RelativeLayout = view.findViewById(R.id.catItemCard)
        var llPhotoHolder: LinearLayout = view.findViewById(R.id.llPhotoHolder)
        var tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val param = catItemCard.layoutParams as ViewGroup.MarginLayoutParams
        val photoHolderParams = llPhotoHolder.layoutParams as ViewGroup.MarginLayoutParams
        val height = (width/9)*16
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 60f
        circularProgressDrawable.start()
        val item = itemsList[position]
        applyPadding(context, holder.param, if (position % NO_OF_ITEMS_IN_CATEGORY_GRID == NO_OF_ITEMS_IN_CATEGORY_GRID - 1) LAST_POS else POS, 1)
        holder.photoHolderParams.height = holder.height
        holder.photoHolderParams.width = width
        Glide.with(context)
            .load(item.link)
            .skipMemoryCache(true)
            .placeholder(circularProgressDrawable)
            .into(holder.itemImageView)

        holder.tvLabel.text = item.subCategory
        holder.tvLabel.post(Runnable {
            val lineCount: Int = holder.tvLabel.lineCount
            if (lineCount >= 2) holder.tvLabel.textSize = 16f
        })
        holder.catItemCard.setOnClickListener {
            val intent = Intent(context, Shelf::class.java)
            intent.putExtra("CategoryLabel", item.subCategory)
            intent.putExtra("Wallies", subCategoryMap[item.subCategory])
            intent.putExtra("AllWallies", allPhotoList)
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return itemsList.size
    }
}