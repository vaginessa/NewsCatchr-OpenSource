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

import com.google.gson.Gson
import io.paperdb.Paper
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.extensions.fromJson
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.extensions.toJson
import jlelse.newscatchr.extensions.tryOrNull

class ArticleCache {

	private val BOOK by lazy { Paper.book("article_cache") }

	fun isCached(id: String): Boolean = BOOK?.exist(id.formatForCache()) ?: false

	fun getById(id: String): Article? = tryOrNull {
		Gson().fromJson<Article>(BOOK?.read<String>(id.formatForCache()) ?: "").apply {
			process(true)
		}
	}

	fun save(article: Article) {
		article.process()
		if (article.originalId.notNullOrBlank() && article.originalId.notNullOrBlank() && !isCached(article.originalId!!)) BOOK?.write(article.originalId!!.formatForCache(), article.toJson())
	}

}