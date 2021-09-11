package xyz.nagdibai.superwallpapers

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import xyz.nagdibai.superwallpapers.databinding.HomeMainBinding

const val BASE_URL = "http://nagdibai.xyz/wally-api/"

class MainActivity : AppCompatActivity() {

    private lateinit var bnd: HomeMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = HomeMainBinding.inflate(layoutInflater)
        val view = bnd.root
        setContentView(view)
        getCurrentData()

        // TODO: 9/11/2021 Remove the hard call
        bnd.menuBtn1.setOnClickListener {
            getCurrentData()
        }
    }

    private fun getCurrentData() {

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WallyApi::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api.getAllWallpapers().awaitResponse()
                if (response.isSuccessful) {

                    val data = response.body()!!

                    withContext(Dispatchers.Main) {
                        bnd.pasteView.text = data[0].link
                        Glide.with(applicationContext)
                            .load(data[0].link)
                            .into(bnd.ivTest)
                    }

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main){
                    Toast.makeText(
                        applicationContext,
                        "Network failure...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode
        if (Build.VERSION.SDK_INT in 21..29) {
            window.statusBarColor = Color.TRANSPARENT
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_LAYOUT_STABLE

        } else if (Build.VERSION.SDK_INT >= 30) {
            window.statusBarColor = Color.TRANSPARENT
            // Making status bar overlaps with the activity
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    // Shows the system bars by removing all the flags
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}