package xyz.nagdibai.superwallpapers

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.nagdibai.superwallpapers.databinding.PhotoWindowSlideBinding
import java.io.*
import java.util.*
import kotlin.concurrent.schedule


const val TAG = "Nagdi"

class PhotoWindow : AppCompatActivity(), DefaultLifecycleObserver  {
    private lateinit var bnd: PhotoWindowSlideBinding

    private lateinit var mAdView : AdView
    private var startIdx = 0
    private lateinit var itemsList: ArrayList<ChitraItem>
    private lateinit var favList: ArrayList<ChitraItem>

    private var cacheDBinMem = true

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var mRewardedAd: RewardedAd? = null
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var options: UCrop.Options
    private val favViewModel: FavViewModel by viewModels {
        FavViewModelFactory((application as FavApp).repository)
    }
    private lateinit var sharedPref: SharedPreferences

    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        bnd = PhotoWindowSlideBinding.inflate(layoutInflater)
        setContentView(bnd.root)

        initAdsAndBanner()
        setViewPager()
        setButtons()
        loadRewardAd()
        loadFullPageAd()
        setActivityCallback()
        setUpUCrop()

    }

    private fun initAdsAndBanner() {
        MobileAds.initialize(this) {}

        mAdView = bnd.bannerAdPhotoWindow
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun setViewPager () {
        itemsList = intent.getSerializableExtra("ItemsList") as ArrayList<ChitraItem>
        favList = ArrayList()
        favViewModel.allFavs.observe(this, Observer { favItems ->
            // Update the cached copy of the words in the adapter.
            if (cacheDBinMem) {
                favItems?.let {
                    favItems.forEach {
                        favList.add(ChitraItem(it._id, it.category, it.subCategory, it.downloads, it.keywords, it.link))
                    }
                }
                cacheDBinMem = false;
            }
        })
        sharedPref = this.getSharedPreferences("pref_s@|t", Context.MODE_PRIVATE)
        startIdx = intent.getIntExtra("StartIdx", 0)
        viewPager = bnd.vpPhoto
        viewPagerAdapter = ViewPagerAdapter(itemsList, this@PhotoWindow)
        viewPager.adapter = viewPagerAdapter
        viewPagerAdapter.notifyDataSetChanged()
        viewPager.currentItem = startIdx

        favViewModel.setFliterQuery(getImgUrl())
        favViewModel.dataToUi.observe(this) { result ->
            result?.apply {
                bnd.fabFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
            }
        }
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) { }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
            override fun onPageSelected(position: Int) {
                if(favList.any{ it.link == itemsList[position].link }){
                    bnd.fabFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                } else {
                    bnd.fabFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }
            }
        })

        downloadTokenUpdate()

    }

    private fun setButtons() {
        bnd.backBtn.setOnClickListener {
            this.finish()
        }

        bnd.fabFavorite.setOnClickListener {
            addToFavorites()
        }

        bnd.fabDownload.setOnClickListener {
            bnd.fabDownloadRemaining.bringToFront()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                downloadPhoto()
            } else {
                checkPermissionAndDownloadBitmap()
            }
        }

        bnd.fabApply.setOnClickListener {
            bnd.frameProgress.visibility = View.VISIBLE
            bnd.loadingCircle.visibility = View.VISIBLE
            Timer().schedule(100){
                cropAndApply()
            }
        }
    }

    private fun getImgUrl(posi: Int = viewPager.currentItem) : String {
        return itemsList[posi].link
    }
    private fun getKeywords() : String {
        return itemsList[viewPager.currentItem].keywords
    }
    private fun getId() : String {
        return itemsList[viewPager.currentItem]._id
    }
    private fun getCategory() : String {
        return itemsList[viewPager.currentItem].category
    }
    private fun getSubCategory() : String {
        return itemsList[viewPager.currentItem].subCategory
    }
    private fun getDownloads() : Int {
        return itemsList[viewPager.currentItem].downloads
    }

    private fun cropAndApply() {
        val currViewPage: ImageView = bnd.vpPhoto.findViewWithTag("viewPagerIvPhoto" + viewPager.currentItem)
        val bitmap = (currViewPage.drawable as BitmapDrawable).bitmap
        val cacheDir = baseContext.cacheDir
        val f = File(cacheDir, "pic")

        try {
            val out = FileOutputStream(f)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            Log.d(TAG, "File saved")
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not in cache ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "IO Error ${e.message}")
        }
        var sourceUri = Uri.fromFile(File(cacheDir, "pic"))
        var fauxDestination = Uri.fromFile(File(cacheDir, "1"))

        var width = ScreenMetrics.getScreenSize(this).width
        var height = ScreenMetrics.getScreenSize(this).height


        val cropper = UCrop.of(sourceUri, fauxDestination)
            .withAspectRatio(width.toFloat(), height.toFloat())
            .withMaxResultSize(width, height)
            .getIntent(this)

        resultLauncher.launch(cropper)
    }

    private fun setActivityCallback() {
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    downloadPhoto()
                    Log.d(TAG, "Permission granted")
                } else {
                    val snack = Snackbar.make(bnd.clPhoto, "Need permission to download to Gallery", Snackbar.LENGTH_INDEFINITE)
                    snack.setAction("Retry", View.OnClickListener {
                        // TODO: Add analytics
                        checkPermissionAndDownloadBitmap()
                    })
                    snack.setActionTextColor(Color.YELLOW)
                    snack.anchorView = bnd.bannerAdPhotoWindow
                    snack.show()
                }
            }

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            bnd.frameProgress.visibility = View.INVISIBLE
            bnd.loadingCircle.visibility = View.INVISIBLE
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                var resultUri: Uri? = data?.let { UCrop.getOutput(it) };
                bnd.frameProgress.visibility = View.INVISIBLE
                bnd.loadingCircle.visibility = View.INVISIBLE
                if (resultUri != null) {
                    applyWallpaper(resultUri)
                } else {
                    Toast.makeText(this, "Error loading data for crop tool", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermissionAndDownloadBitmap() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                downloadPhoto()
                Log.d(TAG, "Permission granted")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                showDialog(
                    "Storage Permission",
                    "Select 'Allow' to give permission to store downloaded photos",
                "OK"
                ) {
                    // TODO: Add analytics
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun saveMediaToStorage() {
        val currViewPage: ImageView = bnd.vpPhoto.findViewWithTag("viewPagerIvPhoto" + viewPager.currentItem)
        val bitmap = (currViewPage.drawable as BitmapDrawable).bitmap
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)

            val firstRun = sharedPref.getBoolean(FIRST_DOWNLOAD, true)

            val snack = Snackbar.make(bnd.clPhoto, "Wallpaper downloaded to your Gallery", Snackbar.LENGTH_INDEFINITE)
            snack.setAction("OK", View.OnClickListener {
                Log.d(TAG, "Download complete dialog dismissed")
                if (firstRun) {
                    showDialog(
                        "Loving the App ?",
                        "help us reach more people by giving us a review",
                        "Rate App",
                    "Later"
                    ) {
                        val packageName = this@PhotoWindow.packageName
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        )
                        startActivity(browserIntent)
                    }
                    with (sharedPref.edit()) {
                        putBoolean(FIRST_DOWNLOAD, false)
                        commit()
                    }
                }
            })

            snack.setTextColor(Color.BLACK)
            snack.setActionTextColor(Color.BLACK)
            snack.setBackgroundTint(Color.rgb(42,202,234))
            snack.anchorView = bnd.bannerAdPhotoWindow
            snack.show()
            downloadIncrement()
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this@PhotoWindow)
            }
        }
    }

    private fun applyWallpaper(imageUri: Uri) {

        var bitmap: Bitmap? = null
        if (Build.VERSION.SDK_INT >= 29) {
            val source: ImageDecoder.Source =
                ImageDecoder.createSource(applicationContext.contentResolver, imageUri)
            try {
                bitmap = ImageDecoder.decodeBitmap(source)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                bitmap =
                    MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, imageUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val wallpaperManager = WallpaperManager.getInstance(baseContext)
        if (bitmap != null) {
            wallpaperManager.setBitmap(bitmap)
//            Toast.makeText(this, "Wallpaper set!", Toast.LENGTH_SHORT).show()
            val snack = Snackbar.make(bnd.clPhoto, "Wallpaper set", Snackbar.LENGTH_INDEFINITE)
            snack.setAction("OK", View.OnClickListener {
                // TODO: Add analytics
            })
            snack.setTextColor(Color.BLACK)
            snack.setActionTextColor(Color.BLACK)
            snack.setBackgroundTint(Color.rgb(42,202,234))
            snack.anchorView = bnd.bannerAdPhotoWindow
            snack.show()
            if(mInterstitialAd != null) {
                mInterstitialAd?.show(this)
            }
        } else {
            Log.e(TAG, "Error loading wallpaper manager")
            Toast.makeText(this, "Error loading wallpaper manager", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadIncrement() {

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WallyApi::class.java)

        val call = api.downloadInc(getString(R.string.collection), "${itemsList[viewPager.currentItem]._id}")

        call.enqueue(object : Callback<ChitraItemAPI> {
            override fun onResponse(call: Call<ChitraItemAPI>, response: Response<ChitraItemAPI>) {
                if (response.code() == 200) {
                    val data = response.body()!!
                    Log.d(TAG, "Downloads incremented to - " + data.downloads)
                } else if (response.code() == 400) {
                    Toast.makeText(this@PhotoWindow, API_FAILURE_MSG, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "API failed 400")
                } else if (response.code() == 500) {
                    Toast.makeText(this@PhotoWindow, API_FAILURE_MSG, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "API failed 500")
                }
            }

            override fun onFailure(call: Call<ChitraItemAPI>, t: Throwable) {
                Toast.makeText(this@PhotoWindow, API_FAILURE_MSG, Toast.LENGTH_SHORT).show()

            }
        })

    }

    private fun downloadPhoto() {
        val downloadsRemaining = sharedPref.getInt(DOWNLOADS_REMAINING, 0)

        if (downloadsRemaining == 0) {
            if (mRewardedAd != null) {
                // TODO: Sentence reconstruction
                showDialog(
                    "Unlock Download",
                    "Watch a video ad to unlock 3 downloads",
                    "Ok",
                    "Cancel"
                ) {
                    mRewardedAd?.show(this@PhotoWindow, OnUserEarnedRewardListener {
                        Log.d(TAG, "User earned the reward for download.")

                        // TODO: Sentence reconstruction
                        val snack = Snackbar.make(bnd.clPhoto, "You are rewarded $DOWNLOADS_REWARDED download tokens ${getEmoji(HEART_EYES_EMOJI)}", Snackbar.LENGTH_INDEFINITE)
                        snack.setAction("OK", View.OnClickListener {
                            Log.d(TAG, "Wallpaper reward dialog dismissed")
                        })
                        snack.setTextColor(Color.BLACK)
                        snack.setActionTextColor(Color.BLACK)
                        snack.setBackgroundTint(Color.GREEN)
                        snack.anchorView = bnd.bannerAdPhotoWindow
                        snack.show()
                        with (sharedPref.edit()) {
                            putInt("downloadsRemaining", DOWNLOADS_REWARDED)
                            commit()
                        }
                        downloadTokenUpdate(3)
                    })
                }
            } else {
                // TODO: Sentence reconstruction
                showDialog(
                    "Ad not loaded",
                    "You have to watch an ad to get download tokens. Please try again in a few seconds. " +
                            "\n\nTIP${getEmoji(STAR_EMOJI)} : You can add the wallpaper to Favorites",
                    "Add To Favorites",
                    "Wait"
                ) {
                    addToFavorites()
                }
            }
        } else {
            with (sharedPref.edit()) {
                putInt("downloadsRemaining", downloadsRemaining - 1)
                commit()
                downloadTokenUpdate(downloadsRemaining - 1)
            }
            Log.d(TAG, "Download allowed")
            saveMediaToStorage()
        }
    }

    private fun addToFavorites() {
        if (favList.any{ it.link == itemsList[viewPager.currentItem].link }){
            bnd.fabFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            favViewModel.delete(getImgUrl())
            val index = favList.indexOfFirst{ it.link == itemsList[viewPager.currentItem].link }
            favList.removeAt(index)
        } else {
            bnd.fabFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
            favViewModel.insert(Favorite(getId(), getCategory(), getSubCategory(), getDownloads(), getKeywords(), getImgUrl()))
            favList.add(ChitraItem(getId(), getCategory(), getSubCategory(), getDownloads(), getKeywords(), getImgUrl()))
        }
    }

    private fun loadRewardAd() {
        var adRequest = AdRequest.Builder().build()

        // TODO: Change the ad id
        RewardedAd.load(
            this,
            getString(R.string.admob_photowindow_download_test),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError?.message)
                    mRewardedAd = null
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mRewardedAd = rewardedAd

                    mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad was shown.")
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            Log.d(TAG, "Ad failed to show.")
                            Toast.makeText(this@PhotoWindow, "Failed to show ad", Toast.LENGTH_SHORT).show()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed whether or not seen entirely")
                            loadRewardAd()
                        }
                    }
                }
            })
    }

    private fun loadFullPageAd() {
        var adRequest = AdRequest.Builder().build()

        // TODO: Change the ad id
        InterstitialAd.load(this,getString(R.string.admob_photowindow_full_test), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
                loadFullPageAd()
            }
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Full page Ad was loaded.")
                mInterstitialAd = interstitialAd
                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                        loadFullPageAd()
                    }
                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                        mInterstitialAd = null
                    }
                }
            }
        })
    }

    private fun setUpUCrop () {
        options = UCrop.Options()
        options.setCompressionQuality(100)
        options.setMaxBitmapSize(10000)
        options.setToolbarColor(ContextCompat.getColor(this, android.R.color.white))
        options.setStatusBarColor(ContextCompat.getColor(this, android.R.color.white))
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, android.R.color.black))
        options.setToolbarWidgetColor(ContextCompat.getColor(this, android.R.color.black))
    }

    private fun downloadTokenUpdate(downloads: Int = -1){
        var downloadsRemaining = downloads
        if(downloadsRemaining == -1) {
            downloadsRemaining = sharedPref.getInt(DOWNLOADS_REMAINING, 0)
        }
        if (downloadsRemaining != 0) {
            bnd.fabDownloadRemaining.text = "$downloadsRemaining"
            bnd.fabDownloadRemaining.visibility = View.VISIBLE
        } else {
            bnd.fabDownloadRemaining.visibility = View.INVISIBLE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

}