/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.backend.apis

import android.net.Uri
import android.support.annotation.Keep
import com.afollestad.bridge.Bridge
import com.afollestad.bridge.annotations.Body
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.extensions.notNullOrEmpty
import jlelse.newscatchr.extensions.tryOrNull

class WordpressApi {

    fun reparse(article: Article?): Pair<Article?, Boolean> {
        return if (article.notNullOrEmpty()) {
            var result = Pair(article, false)
            tryOrNull {
                Bridge.get("https://public-api.wordpress.com/rest/v1.1/sites/${article?.url?.blogDomain()}/posts/slug:${article?.url?.postSlug()}")
                        .asClass(Response::class.java)
                        ?.let {
                            if (it.title.notNullOrBlank() && it.content.notNullOrBlank()) {
                                result = Pair(article?.apply {
                                    title = it.title
                                    content = it.content
                                    if (it.featured_image.notNullOrBlank()) {
                                        enclosure = null
                                        visualUrl = it.featured_image
                                    }
                                    process(true)
                                }, true)
                            }
                        }
            }
            result
        } else Pair(article, false)
    }

    private fun String.blogDomain(): String? = tryOrNull {
        val domain = Uri.parse(this).host
        if (domain.startsWith("www.")) domain.substring(4) else domain
    }

    private fun String.postSlug() = Uri.parse(this).lastPathSegment

    @Keep
    private class Response {
        @Body
        var title: String? = null
        @Body
        var content: String? = null
        @Body
        var featured_image: String? = null
    }

}
