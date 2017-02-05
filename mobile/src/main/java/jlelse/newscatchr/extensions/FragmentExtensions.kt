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
import com.afollestad.json.Ason

fun Bundle.addObject(objectToAdd: Any?, key: String) = tryOrNull { putString(key, Ason().put(key, objectToAdd).toString()) }

fun <T : Any?> Bundle.getObject(key: String, objectClass: Class<T>): T? = tryOrNull { Ason(getString(key)).get(key, objectClass) }

fun Fragment.addTitle(title: String?): Fragment = addObject(title, "ncTitle")

fun Fragment.getAddedTitle(): String? = getAddedString("ncTitle")

fun Fragment.getAddedString(key: String): String? = getAddedObject(key, String::class.java)

fun Fragment.addObject(objectToAdd: Any?, key: String): Fragment {
	val args = arguments ?: Bundle()
	if (args.containsKey(key)) args.remove(key)
	if (objectToAdd != null) args.addObject(objectToAdd, key)
	try {
		arguments = args
	} catch (e: Exception) {
		tryOrNull(execute = e is IllegalStateException) {
			arguments.putAll(args)
		}
	}
	return this
}

fun <T : Any?> Fragment.getAddedObject(key: String, objectClass: Class<T>): T? = if (arguments != null && arguments.containsKey(key)) arguments.getObject(key, objectClass) else null

fun Fragment.sendBroadcast(intent: Intent) = context.sendBroadcast(intent)