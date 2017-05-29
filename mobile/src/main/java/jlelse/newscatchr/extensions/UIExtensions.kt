/*
 * NewsCatchr
 * Copyright © 2017 Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.extensions

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatDelegate
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.readit.R

fun View.hideView() {
	visibility = View.GONE
}

fun View.makeInvisible() {
	visibility = View.INVISIBLE
}

fun View.showView() {
	visibility = View.VISIBLE
}

fun ImageView.loadImage(url: String?) {
	try {
		Glide.with(context).load(url).into(this)
	} catch (e: Exception) {
		e.printStackTrace()
	}
}

fun Context.nothingFound(callback: () -> Unit = {}) {
	MaterialDialog.Builder(this)
			.content(R.string.nothing_found)
			.positiveText(android.R.string.ok)
			.onAny { _, _ -> callback() }
			.cancelListener { callback() }
			.show()
}

fun Context.getPrimaryTextColor(): Int {
	var color = 0
	obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary)).apply {
		color = getColor(0, Color.BLACK)
	}.recycle()
	return color
}

fun View.actionBarSize(): Int {
	val tv = TypedValue()
	if (context.theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
		return TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
	}
	return 0
}

fun setNightMode() {
	AppCompatDelegate.setDefaultNightMode(when (Preferences.nightMode) {
		0 -> AppCompatDelegate.MODE_NIGHT_AUTO
		1 -> AppCompatDelegate.MODE_NIGHT_YES
		else -> AppCompatDelegate.MODE_NIGHT_NO
	})
}

fun TextView.setTextStyle(context: Context, id: Int) {
	@Suppress("DEPRECATION")
	if (Build.VERSION.SDK_INT < 23) setTextAppearance(context, id)
	else setTextAppearance(id)
}