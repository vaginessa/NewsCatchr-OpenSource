/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.backend.helpers

import android.content.Context
import com.bumptech.glide.Glide
import com.mcxiaoke.koi.async.asyncSafe
import com.mcxiaoke.koi.async.mainThreadSafe
import io.paperdb.Paper

fun Any?.saveToCache(key: String?) {
	if (this != null) Paper.book("cache").write(key?.formatForCache(), this)
}

fun <T> readFromCache(key: String?): T = Paper.book("cache").read<T>(key?.formatForCache())

fun String.formatForCache(): String {
	return try {
		replace("[^0-9a-zA-Z]".toRegex(), "")
	} catch (ignored: Exception) {
		this
	}
}

fun Context.clearCache(finished: () -> Unit) {
	asyncSafe {
		Paper.book("cache").destroy()
		Paper.book("article_cache").destroy()
		Glide.get(this@clearCache).clearDiskCache()
		mainThreadSafe {
			finished()
		}
	}
}