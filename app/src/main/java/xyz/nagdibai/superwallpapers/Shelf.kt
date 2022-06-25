package xyz.nagdibai.superwallpapers

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import xyz.nagdibai.superwallpapers.databinding.ShelfBinding


class Shelf : AppCompatActivity() {
    private lateinit var bnd: ShelfBinding
    private lateinit var mAdView : AdView
    private lateinit var photoShelfAdapter: PhotoShelfAdapter
    private lateinit var photoList: ArrayList<ChitraItem>
    private lateinit var allPhotoList: ArrayList<ChitraItem>
    private lateinit var label: String
    private var searchTerm: String? = ""

    private var imgWidth: Int = 0
    private var selfRender: Boolean? = null
    private var isFavCall: Boolean? = null
    private val regex = Regex("[^A-Za-z0-9]")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = ShelfBinding.inflate(layoutInflater)
        setContentView(bnd.root)

        grabBundle()
        setFancyStuff()
        setPhotoShelf()
        initAdsAndBanner()

    }

    private fun grabBundle(){
        label = intent.getStringExtra("CategoryLabel")!!
        searchTerm = intent.getStringExtra("SearchTerm")
        selfRender = intent.getBooleanExtra("SelfRender", false)
        isFavCall = intent.getBooleanExtra("FavCall", false)
        photoList = intent.getSerializableExtra("Wallies") as ArrayList<ChitraItem>
        allPhotoList = intent.getSerializableExtra("AllWallies") as ArrayList<ChitraItem>
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private fun setFancyStuff() {
        bnd.fancyText.text = intent.getStringExtra("CategoryLabel")
        if(photoList.size > 0) {
            Glide.with(baseContext)
                .load(photoList[0].link)
                .into(bnd.fancyImage)
        } else {
            bnd.tvNoMsg.visibility = View.VISIBLE
        }
        bnd.backBtn.setOnClickListener {
            this.finish()
        }
    }

    private fun setPhotoShelf () {
        // List Image Width preset

        val rvPhotoShelf: RecyclerView = bnd.rvPhotoShelf;
        rvPhotoShelf.doOnLayout {
            imgWidth = it.measuredWidth
            rvPhotoShelf.layoutManager = GridLayoutManager(applicationContext,NO_OF_ITEMS_IN_SHELF_GRID)
            photoShelfAdapter = photoList?.let { it1 -> PhotoShelfAdapter(it1, this, (imgWidth/NO_OF_ITEMS_IN_SHELF_GRID)) }!!
            rvPhotoShelf.adapter = photoShelfAdapter
        }
    }

    private fun initAdsAndBanner() {
        MobileAds.initialize(this) {}

        mAdView = bnd.bannerShelf
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

}