package xyz.nagdibai.superwallpapers

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Intent
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.ImageLoader
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import xyz.nagdibai.superwallpapers.PrefData.Keys.FIRST_RUN_REVIEW_DONE
import xyz.nagdibai.superwallpapers.databinding.PhotoWindowBinding
import java.io.*

const val TAG = "PhotoWindow"

class PhotoWindow : AppCompatActivity(), DefaultLifecycleObserver  {
    private lateinit var bnd: PhotoWindowBinding

    private lateinit var mAdView : AdView
    private lateinit var imgURL : String
    private lateinit var keywords : String
    private var downloads = 0
    private lateinit var category: String

    private var isFavorite = false
    private var rewardAdShown = false
    private var showCropperAfterRewardAd = false

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageLoader: ImageLoader
    private var mRewardedAd: RewardedAd? = null
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var options: UCrop.Options
    private val favViewModel: FavViewModel by viewModels {
        FavViewModelFactory((application as FavApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        bnd = PhotoWindowBinding.inflate(layoutInflater)
        setContentView(bnd.root)

        initAdsAndBanner()
        setImageViewAndUI()
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
                Log.d(TAG, result.link)
            }
        }
        imageLoader = Coil.imageLoader(this)
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

        bnd.fabDownload.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                downloadPhoto()
            } else {
                checkPermissionAndDownloadBitmap()
            }
        }

        bnd.fabApply.setOnClickListener {
            cropAndApply()
        }
    }

    private fun cropAndApply() {
        val bitmap = (bnd.ivPhoto.drawable as BitmapDrawable).bitmap
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
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                var resultUri: Uri? = data?.let { UCrop.getOutput(it) };
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
        val bitmap = (bnd.ivPhoto.drawable as BitmapDrawable).bitmap
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

            val firstRunCheck: Flow<Boolean> = dataStore.data
                .map { preferences -> preferences[FIRST_RUN_REVIEW_DONE] ?: true }

            val snack = Snackbar.make(bnd.clPhoto, "Wallpaper downloaded to your Gallery", Snackbar.LENGTH_INDEFINITE)
            snack.setAction("OK", View.OnClickListener {
                Log.d(TAG, "Download complete dialog dismissed")
                lifecycleScope.launch {
                    firstRunCheck.collect() { firstRun ->
                        if (firstRun) {
                            showDialog(
                                "Go Ad Free ?",
                                "Remove the ads by giving us a review on Play Store",
                                "Rate App"
                            ) {
                                val packageName = this@PhotoWindow.packageName
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                )
                                startActivity(browserIntent)
                            }
                            dataStore.edit { prefs -> prefs[FIRST_RUN_REVIEW_DONE] = false }
                        }
                    }
                }
            })
            snack.setTextColor(Color.BLACK)
            snack.setActionTextColor(Color.BLACK)
            snack.setBackgroundTint(Color.GREEN)
            snack.anchorView = bnd.bannerAdPhotoWindow
            snack.show()
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
            snack.setBackgroundTint(Color.GREEN)
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

    private fun downloadPhoto() {
        if (mRewardedAd != null && !rewardAdShown) {
            showDialog(
                "Download",
                "Watch an ad to download the wallpaper to your Gallery?",
                "Ok",
                "Cancel"
            ) {
                mRewardedAd?.show(this, OnUserEarnedRewardListener {
                    Log.d(TAG, "User earned the reward for download.")
                    saveMediaToStorage()
                })
                rewardAdShown = true;
            }
        } else if(mInterstitialAd != null) {
            mInterstitialAd?.show(this)
            saveMediaToStorage()
        } else {
            Log.d(TAG, "Ads not loaded but download allowed")
            Log.d(TAG, "Ads not loaded but download allowed")
            saveMediaToStorage()
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
                }
            })

        mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad was shown.")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Log.d(TAG, "Ad failed to show.")
                Toast.makeText(this@PhotoWindow, "Failed to show ad", Toast.LENGTH_SHORT).show()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.e("Chumbhan", "Ad was dismissed.")
                mRewardedAd = null
            }

        }

    }

    private fun loadFullPageAd() {
        var adRequest = AdRequest.Builder().build()

        // TODO: Change the ad id
        InterstitialAd.load(this,getString(R.string.admob_photowindow_full_test), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Full page Ad was loaded.")
                mInterstitialAd = interstitialAd
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

}