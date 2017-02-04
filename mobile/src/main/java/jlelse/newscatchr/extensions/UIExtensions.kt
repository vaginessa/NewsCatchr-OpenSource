/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.widget.ImageView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.readit.R
import java.util.*

fun View.hideView() {
	visibility = View.GONE
}

fun View.makeInvisible() {
	visibility = View.INVISIBLE
}

fun View.showView() {
	visibility = View.VISIBLE
}

fun ImageView.loadImage(url: String?, crop: Boolean = true) {
	try {
		Glide.with(context).load(url).into(this)
	} catch (e: Exception) {
		e.printStackTrace()
	}
}

fun Context.nothingFound() {
	MaterialDialog.Builder(this)
			.content(R.string.nothing_found)
			.positiveText(android.R.string.ok)
			.show()
}

fun Context.getPrimaryTextColor(): Int {
	return obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary)).getColor(0, Color.BLACK)
}

fun setNightMode() {
	when (Preferences.nightMode) {
		0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
		1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
		2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		3 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
	}
}

fun Context.setLocale() {
	resources.updateConfiguration(resources.configuration.apply {
		val prefLocale = Locale(Preferences.language)
		if (Build.VERSION.SDK_INT >= 17) {
			setLocale(prefLocale)
		} else {
			@Suppress("DEPRECATION")
			locale = prefLocale
		}
		if (this@setLocale is Activity) this@setLocale.recreate()
	}, resources.displayMetrics)
}

fun NestedScrollView.savePosition(fragment: Fragment?) {
	fragment?.addObject(intArrayOf(scrollX, scrollY), "SCROLL_VIEW_POSITION")
}

fun NestedScrollView.restorePosition(fragment: Fragment?) {
	fragment?.getAddedObject<IntArray>("SCROLL_VIEW_POSITION")?.let { post { scrollTo(it[0], it[1]) } }
}

fun Int.dpToPx(): Int {
	val metrics = Resources.getSystem().displayMetrics
	val px = this * (metrics.densityDpi / 160f)
	return Math.round(px)
}