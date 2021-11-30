package lej.happy.fooddiary.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager

// Inconsistency detected. Invalid view holder adapter positionViewHolder 오류 발생하여 추가.
class LinearLayoutManagerWrapper : LinearLayoutManager {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) :
            super(context, orientation, reverseLayout) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {}

    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}

