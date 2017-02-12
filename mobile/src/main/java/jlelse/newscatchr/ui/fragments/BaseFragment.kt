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

package jlelse.newscatchr.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.NestedScrollView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jlelse.newscatchr.extensions.savePosition
import jlelse.newscatchr.extensions.tryOrNull
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.newscatchr.ui.interfaces.FragmentValues

abstract class BaseFragment : Fragment(), FragmentValues {
	override val valueMap = mutableMapOf<String, Any?>()

	lateinit var fragmentNavigation: FragmentNavigation
	open val saveStateScrollViews: Array<NestedScrollView?>? = null

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (activity is MainActivity) (activity as MainActivity).resetToolbarBackground()
		return super.onCreateView(inflater, container, savedInstanceState)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is FragmentNavigation) {
			fragmentNavigation = context
		}
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
	}

	interface FragmentNavigation {
		fun pushFragment(fragment: Fragment, title: String?)
		fun popFragment()
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)
		saveStateScrollViews?.forEach { tryOrNull { it?.savePosition(this) } }
	}

	override fun onPause() {
		saveStateScrollViews?.forEach { tryOrNull { it?.savePosition(this) } }
		super.onPause()
	}
}
