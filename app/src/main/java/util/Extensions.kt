package util

import android.graphics.Bitmap

object Extensions {

}

fun List<String>.commandContain(text: String): Boolean {
    var list = text.trim().split( " ").map { it.trim() }
    val matchFound = list.any { it in this }
    return matchFound
}