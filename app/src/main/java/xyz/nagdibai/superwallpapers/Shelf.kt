package xyz.nagdibai.superwallpapers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import xyz.nagdibai.superwallpapers.databinding.ShelfBinding


class Shelf : AppCompatActivity() {
    private lateinit var bnd: ShelfBinding
    private lateinit var photoShelfAdapter: PhotoShelfAdapter
    private lateinit var photoList: ArrayList<String>
    private lateinit var label: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = ShelfBinding.inflate(layoutInflater)
        setContentView(bnd.root)

        grabBundle()
        setFancyStuff()
        setPhotoShelf()

    }

    fun grabBundle(){
        label = intent.getStringExtra("CategoryLabel")!!
        photoList = intent.getStringArrayListExtra("Wallies")!!
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private fun setFancyStuff() {
        bnd.fancyText.text = intent.getStringExtra("CategoryLabel")
        Glide.with(baseContext)
            .load(photoList[0])
            .into(bnd.fancyImage)
        bnd.backBtn.setOnClickListener {
            this.finish()
        }
    }

    private fun setPhotoShelf () {
        // List Image Width preset
        var imgWidth: Int = 0

        val rvPhotoShelf: RecyclerView = bnd.rvPhotoShelf;
        rvPhotoShelf.doOnLayout {
            imgWidth = it.measuredWidth
            rvPhotoShelf.layoutManager = GridLayoutManager(applicationContext,2)
            photoShelfAdapter = photoList?.let { it1 -> PhotoShelfAdapter(it1, this, (imgWidth/2)) }!!
            rvPhotoShelf.adapter = photoShelfAdapter
        }
    }
}