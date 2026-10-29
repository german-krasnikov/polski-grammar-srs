// Generated from swipe.logic
// source-sha256=4944f9411f45dd1f2c6bbc49b7009b06d7fef2bfd292268e5cfe1aa9479a845f
// Edit the source, then run biome generate.

package polski.interaction

object Policy {
    interface IRatingInputs { fun swipeBelowThreshold(): Boolean; fun swipeDxPositive(): Boolean; fun swipeYDominates(): Boolean }
    object RatingValues { const val Again = "again"; const val Good = "good"; const val None = "none" }
    data class RatingInput(val swipeBelowThreshold: Boolean, val swipeDxPositive: Boolean, val swipeYDominates: Boolean)
    fun rating(input: IRatingInputs): String {
        val value1 = input.swipeBelowThreshold()
        val value0 = input.swipeDxPositive()
        val value2 = input.swipeYDominates()
        return Decision_rating.evaluate(value1, value0, value2)
    }
    fun rating(input: RatingInput): String {
        val value1 = input.swipeBelowThreshold
        val value0 = input.swipeDxPositive
        val value2 = input.swipeYDominates
        return Decision_rating.evaluate(value1, value0, value2)
    }
    interface IInputs : IRatingInputs
    private object Decision_rating {
        fun evaluate(value1: Boolean, value0: Boolean, value2: Boolean): String {
            if (value1 == true) return "none"
            if (value2 == true) return "none"
            if (value0 == true) return "good"
            if (!(value0 == true)) return "again"
            return "none"
        }
    }
}
