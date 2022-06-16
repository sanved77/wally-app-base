package xyz.nagdibai.superwallpapers

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat

fun hideSystemUI(window: Window) {
    // Enables regular immersive mode
    if (Build.VERSION.SDK_INT in 21..29) {
        window.statusBarColor = Color.TRANSPARENT
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    } else if (Build.VERSION.SDK_INT >= 30) {
        window.statusBarColor = Color.TRANSPARENT
        // Making status bar overlaps with the activity
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

}

// Shows the system bars by removing all the flags
fun showSystemUI(window: Window) {
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
}

fun applyPadding(context: Context, param: ViewGroup.MarginLayoutParams, side: Int, big: Int = 16, small: Int = 4, bottom: Int = 0) {
    var dpRatio = context.resources.displayMetrics.density;
    var bigP = (big * dpRatio).toInt()
    var smallP = (small * dpRatio).toInt()
    var bottomP = (bottom * dpRatio).toInt()
    when (side) {
        LEFT -> param.setMargins(bigP,0,smallP,bottomP)
        RIGHT -> param.setMargins(smallP,0,bigP,bottomP)
    }
}

fun Context.showDialog(
    title: String,
    body: String,
    btnText: String = "Retry",
    callback: () -> Unit
) {
    AlertDialog.Builder(this, R.style.AlertDialogTheme).also {
        it.setTitle(title)
        it.setMessage(body)
        it.setPositiveButton(btnText) { _, _ ->
            callback()
        }
    }.create().show()
}