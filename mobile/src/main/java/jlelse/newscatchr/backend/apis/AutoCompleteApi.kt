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

package jlelse.newscatchr.backend.apis

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.afollestad.bridge.Bridge
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.readit.R

class AutoCompleteApi {
	fun getSuggestions(query: String?): List<String?>? {
		if (query.notNullOrBlank()) {
			return Bridge.get("https://duckduckgo.com/ac/?q=%s", query)
					.asClassArray(ResponseItem::class.java)
					?.map { it.phrase }
					?.filter { it.notNullOrBlank() }
		}
		return null
	}

	private class ResponseItem {
		var phrase: String? = null
	}
}

class AutoCompleteAdapter(context: Context) : ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item), Filterable {
	private var api = AutoCompleteApi()
	private var resultList = mutableListOf<String>()

	override fun getCount() = resultList.size

	override fun getItem(index: Int) = resultList[index]

	override fun getFilter(): Filter {
		return object : Filter() {

			override fun performFiltering(constraint: CharSequence?) = FilterResults().apply {
				if (constraint != null) {
					resultList = mutableListOf<String>().apply {
						api.getSuggestions(constraint.toString())?.let { addAll(it) }
					}
					values = resultList
					count = resultList.size
				}
			}

			override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
				if (results != null && results.count > 0) notifyDataSetChanged()
				else notifyDataSetInvalidated()
			}
		}
	}
}

