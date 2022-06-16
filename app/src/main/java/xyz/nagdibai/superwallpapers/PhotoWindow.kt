package xyz.nagdibai.superwallpapers

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import xyz.nagdibai.superwallpapers.PrefData.Keys.FIRST_RUN_REVIEW_DONE
import xyz.nagdibai.superwallpapers.databinding.PhotoWindowBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class PhotoWindow : AppCompatActivity() {
    private lateinit var bnd: PhotoWindowBinding
    private lateinit var mAdView : AdView
    private lateinit var imgURL : String
    private lateinit var keywords : String
    private var downloads = 0
    private lateinit var category: String

    private var isFavorite = false
    private var rewardAdShown = false
    private val favViewModel: FavViewModel by viewModels {
        FavViewModelFactory((application as FavApp).repository)
    }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var imageLoader: ImageLoader
    private var mRewardedAd: RewardedAd? = null
    private var TAG = "PhotoWindow"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = PhotoWindowBinding.inflate(layoutInflater)
        setContentView(bnd.root)

        initAdsAndBanner()
        setImageViewAndUI()
        setButtons()
        loadRewardAd()
        setPermissionCallback()
//        checkPermissionAndDownloadBitmap()

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
    }

    private fun setPermissionCallback() {
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
                        checkPermissionAndDownloadBitmap()
                    })
                    snack.setActionTextColor(Color.YELLOW)
                    snack.show()
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
                    "Permission Required",
                    "Permission required to store downloaded photos to gallery"
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                val snack = Snackbar.make(bnd.clPhoto, "Need permission to download to Gallery", Snackbar.LENGTH_INDEFINITE)
                snack.setAction("Retry", View.OnClickListener {
                    checkPermissionAndDownloadBitmap()
                })
                snack.setActionTextColor(Color.YELLOW)
                snack.show()
            }
        }
    }

    private fun getBitmapFromUrl(bitmapURL: String) = lifecycleScope.launch {

        val request = ImageRequest.Builder(this@PhotoWindow)
            .data(bitmapURL)
            .build()
        try {
            val downloadedBitmap = (imageLoader.execute(request).drawable as BitmapDrawable).bitmap
            saveMediaToStorage(downloadedBitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image " + e.stackTraceToString())
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
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

            val snack = Snackbar.make(bnd.clPhoto, "Photo downloaded to your gallery", Snackbar.LENGTH_INDEFINITE)
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
            snack.show()
        }
    }

    private fun downloadPhoto() {
        if (mRewardedAd != null && !rewardAdShown) {
            mRewardedAd?.show(this, OnUserEarnedRewardListener {
                Log.d(TAG, "User earned the reward.")
                getBitmapFromUrl(imgURL)
            })
            rewardAdShown = true;
        } else {
            Log.d(TAG, "Ad not loaded but download allowed")
            getBitmapFromUrl(imgURL)
        }
    }

    private fun loadRewardAd() {
        var adRequest = AdRequest.Builder().build()

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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }
}