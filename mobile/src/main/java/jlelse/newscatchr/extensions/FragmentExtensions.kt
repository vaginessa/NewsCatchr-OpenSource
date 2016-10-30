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

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

inline fun <reified T> Gson.fromJson(json: String): T = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

fun Bundle.addObject(objectToAdd: Any?, key: String) {
	putString(key, Gson().toJson(objectToAdd))
}

inline fun <reified T> Bundle.getObject(key: String): T? = tryOrNull { Gson().fromJson<T>(getString(key)) }

fun Fragment.addTitle(title: String?): Fragment {
	return addObject(title, "ncTitle")
}

fun Fragment.getAddedTitle(): String? {
	return getAddedString("ncTitle")
}

fun Fragment.addString(stringToAdd: String?, key: String): Fragment {
	return addObject(stringToAdd, key)
}

fun Fragment.getAddedString(key: String): String? {
	return getAddedObject<String>(key)
}

fun Fragment.addObject(objectToAdd: Any?, key: String): Fragment {
	val args = arguments ?: Bundle()
	if (args.containsKey(key)) args.remove(key)
	if (objectToAdd != null) args.addObject(objectToAdd, key)
	try {
		arguments = args
	} catch (e: Exception) {
		if (e is IllegalStateException) {
			tryOrNull {
				arguments.putAll(args)
			}
		} else {
			e.printStackTrace()
		}
	}
	return this
}

inline fun <reified T> Fragment.getAddedObject(key: String): T? {
	return if (arguments != null && arguments.containsKey(key)) arguments.getObject(key)
	else null
}

fun Fragment.sendBroadcast(intent: Intent) {
	context.sendBroadcast(intent)
}