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

package jlelse.newscatchr.ui.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.TextView
import jlelse.newscatchr.backend.helpers.Preferences

class ZoomTextView : TextView {
	var scaleDetector: ScaleGestureDetector? = null
	val zoomLimit = 3.0f

	private var scaleFactor = 1.0f
	private var defaultSize: Float = 0.0f

	constructor(context: Context) : super(context) {
		initialize()
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		initialize()
	}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		initialize()
	}

	private fun initialize() {
		defaultSize = textSize
		scaleDetector = ScaleGestureDetector(context, ScaleListener())
		scaleFactor = Preferences.textScaleFactor
		scaleFactor = Math.max(1.0f, Math.min(scaleFactor, zoomLimit))
		setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultSize * scaleFactor)
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
	}

	override fun onTouchEvent(ev: MotionEvent): Boolean {
		super.onTouchEvent(ev)
		scaleDetector?.onTouchEvent(ev)
		return true
	}

	private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
		override fun onScale(detector: ScaleGestureDetector): Boolean {
			scaleFactor *= detector.scaleFactor
			scaleFactor = Math.max(1.0f, Math.min(scaleFactor, zoomLimit))
			setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultSize * scaleFactor)
			Preferences.textScaleFactor = scaleFactor
			return true
		}
	}
}
