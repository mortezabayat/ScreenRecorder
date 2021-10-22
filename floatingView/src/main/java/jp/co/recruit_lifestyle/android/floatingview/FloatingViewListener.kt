package jp.co.recruit_lifestyle.android.floatingview

interface FloatingViewListener {
    fun onFinishFloatingView()

    /**
     * Callback when touch action finished.
     *
     * @param isFinishing Whether FloatingView is being deleted or not.
     * @param x           x coordinate
     * @param y           y coordinate
     */
    fun onTouchFinished(isFinishing: Boolean, x: Int, y: Int)

}
