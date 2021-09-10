package xyz.nagdibai.superwallpapers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import xyz.nagdibai.superwallpapers.databinding.HomeMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var bnd: HomeMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = HomeMainBinding.inflate(layoutInflater)
        val view = bnd.root
        setContentView(view)
    }
}