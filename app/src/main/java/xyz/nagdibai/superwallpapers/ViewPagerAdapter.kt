package xyz.nagdibai.superwallpapers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import java.util.*


class ViewPagerAdapter(
    private var itemsList: List<ChitraItem>,
    private var context: Context
): PagerAdapter() {

    override fun getCount(): Int {
        return itemsList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView: View = mLayoutInflater.inflate(R.layout.photo, container, false)
        itemView.tag = "page$position"
        val imageView: ImageView = itemView.findViewById<View>(R.id.vpIvPhoto) as ImageView
        imageView.tag = "viewPagerIvPhoto$position"

        Glide.with(context)
            .load(itemsList[position].link)
            .skipMemoryCache(true)
            .into(imageView)

        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }

}