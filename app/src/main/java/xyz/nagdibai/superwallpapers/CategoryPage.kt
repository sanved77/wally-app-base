package xyz.nagdibai.superwallpapers

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import xyz.nagdibai.superwallpapers.databinding.CategoryPageBinding

class CategoryPage : AppCompatActivity() {
    private lateinit var bnd: CategoryPageBinding
    private lateinit var mAdView : AdView
    private lateinit var categoryShelfAdapter: CategoryShelfAdapter
    private lateinit var photoList: ArrayList<ChitraItem>
    private lateinit var categoryList: ArrayList<ChitraItem>
    private lateinit var categoryListBackup: ArrayList<ChitraItem>
    private lateinit var allPhotoList: ArrayList<ChitraItem>
    private lateinit var subCategoryMap: HashMap<String, ArrayList<ChitraItem>>
    private lateinit var label: String
    private var searchTerm: String? = ""

    private var imgWidth: Int = 0
    private var selfRender: Boolean? = null
    private var isFavCall: Boolean? = null
    private val regex = Regex("[^A-Za-z0-9]")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = CategoryPageBinding.inflate(layoutInflater)
        setContentView(bnd.root)

        grabBundle()
        setFancyStuff()
        initData()
        initSubCategories()
        initAdsAndBanner()
        setUpSearch()

    }

    private fun grabBundle(){
        label = intent.getStringExtra("CategoryLabel")!!
        searchTerm = intent.getStringExtra("SearchTerm")
        selfRender = intent.getBooleanExtra("SelfRender", false)
        isFavCall = intent.getBooleanExtra("FavCall", false)
        photoList = intent.getSerializableExtra("SubCategoryWallies") as ArrayList<ChitraItem>
        allPhotoList = intent.getSerializableExtra("AllWallies") as ArrayList<ChitraItem>
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private fun setFancyStuff() {
        val catLabel = intent.getStringExtra("CategoryLabel")
        bnd.fancyText.text = catLabel
        bnd.etSearch.hint ="Search in $catLabel"
        Glide.with(baseContext)
            .load(photoList[0].link)
            .into(bnd.fancyImage)

        bnd.backBtn.setOnClickListener {
            this.finish()
        }
    }

    private fun initData() {
        photoList = intent.getSerializableExtra("SubCategoryWallies") as ArrayList<ChitraItem>
        categoryList = ArrayList()
        categoryListBackup = ArrayList()
        subCategoryMap = HashMap()
        photoList.forEach{
            if(!subCategoryMap.containsKey(it.subCategory))
                subCategoryMap[it.subCategory] = ArrayList()

            subCategoryMap[it.subCategory]?.add(it)
            if(subCategoryMap[it.subCategory]?.size == 1)
                categoryList.add(it)
        }

        categoryListBackup.addAll(categoryList)

    }

    private fun initSubCategories () {
        val rvCategoryShelf: RecyclerView = bnd.rvCategoryShelf;
        rvCategoryShelf.doOnLayout {
            imgWidth = it.measuredWidth
            rvCategoryShelf.layoutManager = GridLayoutManager(applicationContext, NO_OF_ITEMS_IN_CATEGORY_GRID)

            categoryShelfAdapter = categoryList?.let { it1 -> CategoryShelfAdapter(it1, subCategoryMap, allPhotoList, this, (imgWidth/NO_OF_ITEMS_IN_CATEGORY_GRID)) }!!
            rvCategoryShelf.adapter = categoryShelfAdapter
        }
    }

    private fun initAdsAndBanner() {
        MobileAds.initialize(this) {}

        mAdView = bnd.bannerShelf
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun setUpSearch() {
        if(searchTerm != ""){
            bnd.etSearch.setText(searchTerm)
            bnd.etSearch.setSelection(bnd.etSearch.length())
        }
        bnd.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchThisShit()
                return@OnEditorActionListener true
            }
            false
        })
        bnd.btnSearch.setOnClickListener {
            searchThisShit()
        }
        bnd.btnClearSearch.setOnClickListener{
            bnd.btnClearSearch.visibility = View.GONE
            bnd.etSearch.setText("")
            categoryList.clear()
            categoryList.addAll(categoryListBackup)
            categoryShelfAdapter.notifyDataSetChanged()
        }
    }

    private fun searchThisShit() {
        bnd.tvNoMsg.visibility = View.GONE
        bnd.btnClearSearch.visibility = View.VISIBLE
        hideKeyboard(currentFocus ?: View(this))
        var term = bnd.etSearch.text.toString()
        term = term.lowercase().replace(" ", "")
        term = regex.replace(term, "")

        if (term == "") {
            bnd.etSearch.error = "Enter something to search"
        } else {

            val searchResultList = ArrayList<ChitraItem>()

            categoryList?.forEach {
                if (it.subCategory.lowercase().contains(term) || term.contains(it.subCategory.lowercase())) {
                    searchResultList.add(it)
                }
            }

            categoryList.clear();
            categoryList.addAll(searchResultList)
            if (searchResultList.size == 0) {
                bnd.tvNoMsg.visibility = View.VISIBLE
            }
            categoryShelfAdapter.notifyDataSetChanged()
        }
    }

}