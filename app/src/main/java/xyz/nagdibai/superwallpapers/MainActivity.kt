package xyz.nagdibai.superwallpapers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.MobileAds
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.nagdibai.superwallpapers.databinding.HomeMainBinding


const val BASE_URL = "http://nagdibai.xyz/wally-api/"
const val API_FAILURE_MSG = "Failed to load wallpapers"

class MainActivity : AppCompatActivity() {

    private lateinit var bnd: HomeMainBinding
    private val popularItemsList = ArrayList<ChitraItem>()
    private val categoryItemsList = ArrayList<ChitraItem>()
    private val favoriteItemsList = ArrayList<ChitraItem>()
    private lateinit var popularListAdapter: PopularListAdapter
    private lateinit var categoryListAdapter: CategoryListAdapter
    private var categoryMap = HashMap<String, ArrayList<ChitraItem>>()
    private val favViewModel: FavViewModel by viewModels {
        FavViewModelFactory((application as FavApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        bnd = HomeMainBinding.inflate(layoutInflater)
        val view = bnd.root
        setContentView(view)

        MobileAds.initialize(this) {}

        setUpImageLists()
        grabThemWallpapers()
        loadUpFavorites()
        setUpSearch()

    }
    
    private fun setUpImageLists() {
        // List Image height preset
        var imgHeight: Int

        // Popular Images
        val rvPopular: RecyclerView = bnd.rvPopular
        rvPopular.doOnLayout {
            imgHeight = it.measuredHeight
            popularListAdapter = PopularListAdapter(popularItemsList, this, imgHeight)
            rvPopular.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rvPopular.adapter = popularListAdapter
        }

        // Categories
        val rvCategories: RecyclerView = bnd.rvCategories
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

        val call = api.getAllWallpapers(getString(R.string.collection))

        call.enqueue(object : Callback<Chitra> {
            override fun onResponse(call: Call<Chitra>, response: Response<Chitra>) {
                if (response.code() == 200) {
                    val data = response.body()!!
                    categoryMap["All"] = ArrayList<ChitraItem>()

                    var popularSorted = data.sortedWith(compareBy {it.downloads}).asReversed()

                    for (i in data.indices) {
                        if (i in 0..4)
                            popularItemsList.add(
                                ChitraItem(
                                    popularSorted[i]._id,
                                    popularSorted[i].category,
                                    popularSorted[i].subCategory,
                                    popularSorted[i].downloads,
                                    popularSorted[i].keywords,
                                    popularSorted[i].link
                                )
                            )
                        if (!categoryMap.containsKey(data[i].category))
                            categoryMap[data[i].category] = ArrayList<ChitraItem>()
                        var tempChitra = ChitraItem(
                            data[i]._id,
                            data[i].category,
                            data[i].subCategory,
                            data[i].downloads,
                            data[i].keywords,
                            data[i].link
                        )
                        categoryMap[data[i].category]?.add(tempChitra)
                        categoryMap["All"]?.add(tempChitra)
                    }
                    popularListAdapter.notifyDataSetChanged()

                    categoryMap.forEach {
                        categoryItemsList.add(
                            ChitraItem(
                                it.value[0]._id,
                                it.key,
                                it.value[0].subCategory,
                                it.value[0].downloads,
                                it.value[0].keywords,
                                it.value[0].link
                            )
                        )
                    }
                    categoryListAdapter.notifyDataSetChanged()

                } else if (response.code() == 400) {
                    Toast.makeText(this@MainActivity, API_FAILURE_MSG, Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "API failed 400")
                } else if (response.code() == 500) {
                    Toast.makeText(this@MainActivity, API_FAILURE_MSG, Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "API failed 500")
                }
            }

            override fun onFailure(call: Call<Chitra>, t: Throwable) {
                Toast.makeText(this@MainActivity, API_FAILURE_MSG, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun loadUpFavorites() {
        favViewModel.allFavs.observe(this) { favList ->
            favList?.let { fav ->
                favoriteItemsList.clear()
                fav.forEach {
                    favoriteItemsList.add(
                        ChitraItem(
                            it._id,
                            it.category,
                            it.subCategory,
                            it.downloads,
                            it.keywords,
                            it.link
                        )
                    )
                }
            }
        }

        bnd.btnSavedWallpapers.setOnClickListener {
            val intent = Intent(this, Shelf::class.java)
            intent.putExtra("CategoryLabel", "Saved")
            intent.putExtra("Wallies", favoriteItemsList)
            intent.putExtra("AllWallies", categoryMap["All"])
            intent.putExtra("FavCall", true)
            startActivity(intent)
        }
    }
    
    private fun setUpSearch() {
        bnd.etSearch.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchThisShit()
                return@OnEditorActionListener true
            }
            false
        })
        bnd.btnSearch.setOnClickListener {
            searchThisShit()
        }
    }

    private fun searchThisShit() {
        val term = bnd.etSearch.text.toString()

        if (term == "") {
            bnd.etSearch.error = "Enter something to search"
        } else {
            val searchResultList = ArrayList<ChitraItem>()

            categoryMap["All"]?.forEach {
                if (it.keywords.contains(term)) {
                    searchResultList.add(it)
                }
            }
            val intent = Intent(this, Shelf::class.java)
            intent.putExtra("CategoryLabel", "Search")
            intent.putExtra("SearchTerm", term)
            intent.putExtra("SelfRender", true)
            intent.putExtra("Wallies", searchResultList)
            intent.putExtra("AllWallies", categoryMap["All"])
            startActivity(intent)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

}