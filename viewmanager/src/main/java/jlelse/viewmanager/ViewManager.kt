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

package jlelse.viewmanager

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout

abstract class ViewManagerActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		viewManagerActivity = this
		if (viewStacks.size == 0) viewStacks += initViewStacks
	}

	abstract val initViewStacks: MutableList<MutableList<ViewManagerView>>
	abstract val containerView: FrameLayout

	fun currentView(): ViewManagerView = viewStacks[currentStack].last()
	private fun allViews() = viewStacks.flatten()

	open fun onSwitchView() {
	}

	fun switchStack(stack: Int) {
		currentStack = stack
		switchView(viewStacks[stack].last())
	}

	fun openView(view: ViewManagerView) {
		view.title = view.title ?: currentView().title ?: ""
		viewStacks[currentStack].add(view)
		switchView(view)
	}

	fun closeView() {
		viewStacks[currentStack].apply {
			if (size > 1) removeAt(lastIndex).onDestroy()
			switchView(last())
		}
	}

	fun resetStack() {
		resetStack(currentStack)
	}

	fun isRootView() = viewStacks[currentStack].size == 1
	fun currentStack() = currentStack

	private fun resetStack(stack: Int) {
		viewStacks[stack].apply { while (size > 1) removeAt(lastIndex).onDestroy() }
		switchStack(stack)
	}

	private fun switchView(view: ViewManagerView?) {
		if (view != null) containerView.apply {
			removeAllViews()
			view.apply { (parent as ViewGroup?)?.removeView(this) }
			addView(view.apply { onShow() })
			invalidateOptionsMenu()
			onSwitchView()
		}
	}

	override fun onBackPressed() = when {
		viewStacks[currentStack].size > 1 -> closeView()
		currentStack != 0 -> switchStack(0)
		else -> super.onBackPressed()
	}

	override fun onDestroy() {
		containerView.removeAllViews()
		allViews().forEach { it.onDestroy() }
		super.onDestroy()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		super.onCreateOptionsMenu(menu)
		menu?.clear()
		createMenu(menu)
		currentView().inflateMenu(menuInflater, menu)
		return true
	}

	open fun createMenu(menu: Menu?) {
	}

	override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
		super.onPrepareOptionsMenu(menu)
		menu?.clear()
		createMenu(menu)
		currentView().inflateMenu(menuInflater, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		super.onOptionsItemSelected(item)
		currentView().onOptionsItemSelected(item)
		return true
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		currentView().onActivityResult(requestCode, resultCode, data)
	}

	override fun recreate() {
		allViews().forEach { it.resetView() }
		super.recreate()
	}
}

private class DummyViewManagerActivity : ViewManagerActivity() {
	override val initViewStacks: MutableList<MutableList<ViewManagerView>>
		get() = mutableListOf()
	override val containerView: FrameLayout
		get() = FrameLayout(this)
}

abstract class ViewManagerView : LinearLayout(viewManagerActivity) {
	val contentView by lazy { this }
	val context
		get() = viewManagerActivity
	var title: String? = null

	init {
		orientation = LinearLayout.VERTICAL
	}

	fun withTitle(title: String?): ViewManagerView {
		this.title = title
		return this
	}

	fun onShow() {
		if (contentView.childCount == 0) contentView.addView(onCreateView())
	}

	open fun onCreateView(): View? {
		return null
	}

	open fun onDestroy() {}

	fun openView(view: ViewManagerView) {
		context.openView(view)
	}

	fun closeView() {
		context.closeView()
	}

	fun resetView() {
		contentView.removeAllViews()
	}

	open fun inflateMenu(inflater: MenuInflater, menu: Menu?) {
	}

	open fun onOptionsItemSelected(item: MenuItem?) {
	}

	open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	}
}

private val viewStacks = mutableListOf<MutableList<ViewManagerView>>()
private var currentStack: Int = 0
private var viewManagerActivity: ViewManagerActivity = DummyViewManagerActivity()