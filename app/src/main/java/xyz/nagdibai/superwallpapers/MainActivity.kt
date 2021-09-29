package xyz.nagdibai.superwallpapers

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.CategoryListAdapter
import com.example.myapplication.PopularListAdapter
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
    private val popularItemsList = ArrayList<String>()
    private val categoryItemsList = ArrayList<CategoryListItem>()
    private lateinit var popularListAdapter: PopularListAdapter
    private lateinit var categoryListAdapter: CategoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Layout setup
        bnd = HomeMainBinding.inflate(layoutInflater)
        val view = bnd.root
        setContentView(view)

        // List Image height preset
        var imgHeight: Int = 0

        // Popular Images
        val rvPopular: RecyclerView = bnd.rvPopular;
        rvPopular.doOnLayout {
            imgHeight = it.measuredHeight
            popularListAdapter = PopularListAdapter(popularItemsList, this, imgHeight)
            rvPopular.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rvPopular.adapter = popularListAdapter
        }

        // Categories
        val rvCategories: RecyclerView = bnd.rvCategories;
        rvCategories.doOnLayout {
            imgHeight = it.measuredHeight
            categoryListAdapter = CategoryListAdapter(categoryItemsList, this, imgHeight)
            rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rvCategories.adapter = categoryListAdapter
        }

        // Grabbing data
        getCurrentData()

        bnd.menuBtn.setOnClickListener {
            rvPopular.layoutParams;
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
                val response = api.getAllWallpapers(getString(R.string.collection)).awaitResponse()
                if (response.isSuccessful) {

                    val data = response.body()!!

                    var popularSorted = data.sortedWith(compareBy {it.downloads}).asReversed()
                    val categoryMap = HashMap<String, ArrayList<String>>()

                    withContext(Dispatchers.Main) {
                        for (i in data.indices) {
                            if (i in 0..4)
                                popularItemsList.add(popularSorted[i].link);
                            if (!categoryMap.containsKey(data[i].category))
                                categoryMap[data[i].category] = ArrayList<String>()
                            categoryMap[data[i].category]?.add(data[i].link);
                        }
                        popularListAdapter.notifyDataSetChanged()

                        categoryMap.forEach{
                            categoryItemsList.add(CategoryListItem(it.key, it.value[1]))
                        }

                        categoryListAdapter.notifyDataSetChanged()
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

        // List Image size presets
        var imgH: Int = 0

    }

    // Shows the system bars by removing all the flags
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}