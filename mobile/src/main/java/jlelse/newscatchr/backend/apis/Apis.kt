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
import android.content.Intent
import jlelse.newscatchr.extensions.resStr
import jlelse.newscatchr.ui.views.ProgressDialog
import jlelse.readit.R
import org.jetbrains.anko.runOnUiThread

// Share
fun Context.share(title: String, text: String) {
	val progressDialog: ProgressDialog = ProgressDialog(this).apply { show() }
	startActivity(Intent.createChooser(Intent().apply {
		action = Intent.ACTION_SEND
		type = "text/plain"
		putExtra(Intent.EXTRA_SUBJECT, title)
		putExtra(Intent.EXTRA_TEXT, text)
	}, "${R.string.share.resStr()} $title"))
	runOnUiThread { progressDialog.dismiss() }
}
