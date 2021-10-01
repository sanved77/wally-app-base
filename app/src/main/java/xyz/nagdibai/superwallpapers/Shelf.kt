package xyz.nagdibai.superwallpapers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.PhotoShelfAdapter
import xyz.nagdibai.superwallpapers.databinding.ShelfBinding


class Shelf : AppCompatActivity() {
    private lateinit var bnd: ShelfBinding
    private lateinit var photoShelfAdapter: PhotoShelfAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = ShelfBinding.inflate(layoutInflater)
        setContentView(bnd.root)

        setFancyStuff()

        setPhotoShelf()

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private fun setFancyStuff() {
        bnd.fancyText.text = intent.getStringExtra("CategoryLabel")
        bnd.backBtn.setOnClickListener {
            this.finish()
        }
    }

    private fun setPhotoShelf () {
        val popularItemsList = intent.getStringArrayListExtra("Wallies")

        // List Image Width preset
        var imgWidth: Int = 0

        val rvPhotoShelf: RecyclerView = bnd.rvPhotoShelf;
        rvPhotoShelf.doOnLayout {
            imgWidth = it.measuredWidth
            rvPhotoShelf.layoutManager = GridLayoutManager(applicationContext,2)
            photoShelfAdapter = popularItemsList?.let { it1 -> PhotoShelfAdapter(it1, this, (imgWidth/2)) }!!
            rvPhotoShelf.adapter = photoShelfAdapter
        }
    }
}