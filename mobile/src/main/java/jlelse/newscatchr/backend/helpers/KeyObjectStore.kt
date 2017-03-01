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

package jlelse.newscatchr.backend.helpers

import com.afollestad.ason.Ason
import com.afollestad.ason.AsonArray
import jlelse.newscatchr.appContext
import jlelse.newscatchr.extensions.tryOrNull
import java.io.File

class KeyObjectStore(name: String = "default") {
	private var file: File = File(appContext?.filesDir, "NCStore$name.nc")
	private var ason: Ason

	init {
		if (file.exists()) ason = tryOrNull { Ason(file.readText()) } ?: Ason()
		else {
			file.createNewFile()
			ason = Ason()
		}
	}

	fun <T> write(key: String?, item: T?): KeyObjectStore {
		if (item == null) delete(key)
		else if (key != null && key.isNotBlank()) {
			tryOrNull { ason.put(key, Ason.serialize(item)) }
			tryOrNull { file.writeText(ason.toString()) }
		}
		return this
	}

	fun <T> write(key: String?, item: Array<T?>?): KeyObjectStore {
		if (item == null) delete(key)
		else if (key != null && key.isNotBlank()) {
			tryOrNull { ason.put(key, Ason.serializeArray<T>(item)) }
			tryOrNull { file.writeText(ason.toString()) }
		}
		return this
	}

	fun delete(key: String?): KeyObjectStore {
		if (key != null && key.isNotBlank()) {
			tryOrNull { ason.remove(key) }
			tryOrNull { file.writeText(ason.toString()) }
		}
		return this
	}

	fun <T> read(key: String?, type: Class<T>): T? = if (key != null) tryOrNull {
		if (type.isArray) ason.get<AsonArray<T>>(key)?.deserialize(type)
		else ason.get<Ason>(key)?.deserialize(type)
	} else null

	fun destroy() {
		tryOrNull { file.writeText("") }
		ason = Ason()
	}

	fun exists(key: String?): Boolean = if (key != null && key.isNotBlank()) ason.has(key) else false

}