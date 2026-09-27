package org.isoron.uhabits.core.models

enum class HabitType(val value: Int) {
    YES_NO(0), NUMERICAL(1), QUIT(2);

    companion object {
        fun fromInt(value: Int): HabitType {
            return when (value) {
                YES_NO.value -> YES_NO
                NUMERICAL.value -> NUMERICAL
                QUIT.value -> QUIT
                else -> throw IllegalStateException()
            }
        }
    }
}
