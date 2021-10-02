package xyz.nagdibai.superwallpapers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private var categoryMap = HashMap<String, ArrayList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = HomeMainBinding.inflate(layoutInflater)
        val view = bnd.root
        setContentView(view)

        setUpImageLists()
        grabThemWallpapers()

    }

    private fun setUpImageLists() {
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
            categoryListAdapter = CategoryListAdapter(categoryItemsList, categoryMap, this, imgHeight)
            rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rvCategories.adapter = categoryListAdapter
        }
    }

    private fun grabThemWallpapers() {

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
                    categoryMap["All"] = ArrayList<String>()

                    var popularSorted = data.sortedWith(compareBy {it.downloads}).asReversed()

                    withContext(Dispatchers.Main) {
                        for (i in data.indices) {
                            if (i in 0..4)
                                popularItemsList.add(popularSorted[i].link);
                            if (!categoryMap.containsKey(data[i].category))
                                categoryMap[data[i].category] = ArrayList<String>()
                            categoryMap[data[i].category]?.add(data[i].link);
                            categoryMap["All"]?.add(data[i].link);
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
        if (hasFocus) hideSystemUI(window)
    }

}