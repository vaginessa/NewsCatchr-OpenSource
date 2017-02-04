/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.ui.views

import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import jlelse.newscatchr.extensions.addString
import jlelse.newscatchr.extensions.tryOrNull
import jlelse.newscatchr.ui.fragments.BaseFragment
import jlelse.newscatchr.ui.fragments.MixFragment
import jlelse.readit.R
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick

fun FlexboxLayout.addTagView(fragment: BaseFragment, tagString: String?) = tryOrNull {
	addView(LayoutInflater.from(fragment.context).inflate(R.layout.tagitem, null)?.apply {
		find<TextView>(R.id.tagView).apply {
			text = "#$tagString"
			onClick {
				fragment.fragmentNavigation.pushFragment(MixFragment().addString("topic/$tagString", "feedId"), "#$tagString")
			}
		}
	})
}