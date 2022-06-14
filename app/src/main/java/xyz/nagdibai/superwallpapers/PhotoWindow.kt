package xyz.nagdibai.superwallpapers

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch
import xyz.nagdibai.superwallpapers.databinding.PhotoWindowBinding

class PhotoWindow : AppCompatActivity() {
    private lateinit var bnd: PhotoWindowBinding
    private lateinit var mAdView : AdView
    private lateinit var imgURL : String
    private lateinit var keywords : String
    private var downloads = 0
    private lateinit var category: String
    private var isFavorite = false
    private val favViewModel: FavViewModel by viewModels {
        FavViewModelFactory((application as FavApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = PhotoWindowBinding.inflate(layoutInflater)
        setContentView(bnd.root)

        initAdsAndBanner()
        setImageViewAndUI()
        setButtons()

    }

    private fun initAdsAndBanner() {
        MobileAds.initialize(this) {}

        mAdView = bnd.bannerAdPhotoWindow
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun setImageViewAndUI() {
        imgURL = intent.getStringExtra("link")!!
        keywords = intent.getStringExtra("keywords")!!
        downloads = intent.getIntExtra("downloads", 0)
        category = intent.getStringExtra("category")!!
        Glide.with(baseContext)
            .load(imgURL)
            .into(bnd.ivPhoto)

        favViewModel.setFliterQuery(imgURL)
        favViewModel.dataToUi.observe(this) { result ->
            result?.apply {
                bnd.fabFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                isFavorite = true
                Log.i("Favorited", result.link)
            }
        }
    }

    private fun setButtons() {
        bnd.backBtn.setOnClickListener {
            this.finish()
        }
        bnd.fabFavorite.setOnClickListener {
            if (isFavorite){
                bnd.fabFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                isFavorite = false
                favViewModel.delete(imgURL)
            } else {
                bnd.fabFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                isFavorite = true
                favViewModel.insert(Favorite(category, downloads, keywords, imgURL))
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }
}