package jlelse.newscatchr.ui.views

import android.content.Context
import android.text.InputType
import android.widget.AutoCompleteTextView
import org.jetbrains.anko.lines

class SearchDialogView(context: Context) : AutoCompleteTextView(context) {

	init {
		threshold = 3
		inputType = InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
		lines = 1
		maxLines = 1
		minLines = 1
		requestFocus()
	}

}